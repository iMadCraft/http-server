package com.kskb.se.http;

import com.kskb.se.base.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

import static java.util.logging.Level.FINE;

class HttpServerImpl implements HttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerImpl.class);
    private static final int MAX_ERROR = 10;
    private static final Map<String, Class<? extends HttpResource>> RESOURCE_MAP = new HashMap<>();

    static {
        RESOURCE_MAP.put("html", HttpHyperText.class);
        RESOURCE_MAP.put("css", HttpStyle.class);
        RESOURCE_MAP.put("js", HttpScript.class);
        RESOURCE_MAP.put("ico", HttpIconImage.class);
    }

    private final int port;
    private final int backlog;
    private final InetAddress addr;
    private final HttpParser parser;
    private final HttpSerializer serializer;
    private final HttpResourceLocator locator;
    private final String trustStoreName;
    private final String keyStoreName;
    private final String tlsVersion;
    
    private final HttpEndPoints endPoints = HttpEndPoints.create();
    private final HttpRewriters rewriters = HttpRewriters.create();
    private final HttpResourceLoader loader;
    private final boolean requireClientAuthentication;
    private final SessionManager sessionManager;
    private final HttpErrorHandlers errorHandlers;
    private final HttpHooks hooks = HttpHooks.create();

    // Sensitive information will be sanitized after usage
    private char[] trustStorePassword;
    private char[] keyStorePassword;

    private HttpServerState state = HttpServerState.INITIALIZED;
    private ServerSocket serverSocket;

    HttpServerImpl(HttpServerContext context) {
        this.port = context.port();
        this.backlog = context.backlog();
        this.addr = context.addr();
        this.trustStoreName = context.trustStoreName();
        this.trustStorePassword = context.trustStorePassword();
        this.keyStoreName = context.keyStoreName();
        this.keyStorePassword = context.keyStorePassword();
        this.tlsVersion = context.tlsVersion();
        this.requireClientAuthentication = context.requireClientAuthentication();
        this.locator = context.locator();
        this.loader = context.loader();
        this.errorHandlers = context.errorHandlers();
        this.parser = context.parser()
           .orElse(new HttpParserImpl());
        this.serializer = context.serializer()
           .orElse(new HttpSerializerImpl());
        this.sessionManager = context.sessionManager() != null ?
           context.sessionManager() : new SessionManagerImpl();
    }

    @Override
    public void start() throws HttpServerException {
        state = HttpServerState.STARTED;
        serverSocket = createServerSocket();
        state = HttpServerState.RUNNING;
        run();
    }

    @Override
    public void stop() throws HttpServerException {
        state = HttpServerState.STOP;
    }

    @Override
    public HttpEndPoints endPoints() {
        return this.endPoints;
    }

    @Override
    public HttpRewriters rewriters() {
        return this.rewriters;
    }

    @Override
    public HttpResourceLoader resourceLoader() {
        return this.loader;
    }

    @Override
    public HttpServerState state() {
        return this.state;
    }

    @Override
    public HttpHooks hooks() {
        return this.hooks;
    }

    private void sanitize() {
        this.trustStorePassword = null;
        this.keyStorePassword = null;
    }

    private ServerSocket createEncryptedServerSocket() throws Exception {
        assert trustStoreName != null && ! trustStoreName.isEmpty();
        assert trustStorePassword != null && trustStorePassword.length != 0;
        assert keyStoreName != null && ! keyStoreName.isEmpty();
        assert keyStorePassword != null && keyStorePassword.length != 0;

        final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final InputStream trustStoreStream = loader.load(HttpSecretResource.class, trustStoreName)
           .orElseThrow()
           .stream();
        trustStore.load(trustStoreStream, trustStorePassword);
        assert trustStoreStream != null;
        trustStoreStream.close();
        final TrustManagerFactory trustManagerFactory =
           TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        final KeyStore keyStore =
           KeyStore.getInstance(KeyStore.getDefaultType());
        final InputStream keyStoreStream = loader.load(HttpSecretResource.class, keyStoreName)
           .orElseThrow()
           .stream();
        keyStore.load(keyStoreStream, keyStorePassword);
        final KeyManagerFactory keyManagerFactory =
           KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagerFactory.getKeyManagers(),
           trustManagerFactory.getTrustManagers(),
           SecureRandom.getInstanceStrong());
        SSLServerSocketFactory factory = ctx.getServerSocketFactory();
        SSLServerSocket ss =
           (SSLServerSocket) factory.createServerSocket(port, backlog, addr);

        ss.setNeedClientAuth(requireClientAuthentication);
        ss.setEnabledProtocols(new String[] {tlsVersion});
        sanitize();
        return ss;
    }

    private ServerSocket createUnencrypedServerSocket() throws IOException {
        return new ServerSocket(port, backlog, addr);
    }

    private ServerSocket createServerSocket() throws HttpServerException {
        final ServerSocket serverSocket;
        try {
            if (trustStoreName != null && ! trustStoreName.isEmpty()) {
                LOG.log(Level.INFO, () -> "Starting https server listening to port " + port);
                serverSocket = createEncryptedServerSocket();
            }
            else {
                LOG.log(Level.INFO, () -> "Starting http server listening to port " + port);
                serverSocket = createUnencrypedServerSocket();
            }
        } catch (Exception e) {
            throw new HttpServerException("Unable to start server", e);
        }
        return serverSocket;
    }

    private void run() throws HttpServerException {
        // Used for a simple infinite error-loop prevention,
        // reset after a successful attempt
        int errorCounter = 0;
        HttpConnection connection = null;

        final HttpErrorHandlerContext.Builder errorBuilder = HttpErrorHandlerContext.builder();

        // Main Loop. Following flow:
        //    * Accept client request
        //    * Parse request
        //    * Modify request (rewriters)
        //    * Create response builder with server defaults
        //    * Find matching endpoints and forward request and responseBuilder
        while (isRunning()) {
            try {
                // Reset state of error handler
                errorBuilder.clear();

                if (connection != null && connection.keepAlive()) {
                    LOG.log(Level.INFO, () -> "Relistening to client ...");
                }
                else {
                    LOG.log(Level.INFO, () -> "Listening to client ...");
                    // Blocking call. Wait until a client socket has been accepted
                    connection = accept();
                }

                final var requestBuilder = HttpRequestImpl.builder();
                final var responseBuilder = HttpResponseImpl.builder();
                final var date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
                   .format(Calendar.getInstance().getTime());
                final var flow = HttpFlowController.create();
                final var hooks = this.hooks.clone();

                // Run initial hooks
                if (flow.shouldInitialize()) {
                    final var initialHookContext = HttpInitialHookContext.builder()
                       .withFlowController(flow)
                       .withRequest(requestBuilder)
                       .withResponse(responseBuilder)
                       .build();
                    for (final HttpHook<HttpInitialHookContext> hook : hooks.byType(HttpInitialHookContext.class)) {
                        hook.run(initialHookContext);

                        if(flow.isInterrupted())
                            break;
                    }
                }

                // Parse incoming stream and populate request builder
                if (flow.shouldParse()) {
                    if(parser.parse(requestBuilder, connection.input())) {
                        LOG.log(Level.INFO, () -> "Request " +
                           requestBuilder.method().name() + " " + requestBuilder.uri());
                    }
                    else {
                        // Error is handled by parser, just
                        // continue with the next request
                        continue;
                    }
                }

                // Extract cookies from request
                final List<HttpHeader> cookieHeaders = requestBuilder.headers().stream()
                   .filter(header -> "Cookie".equals(header.name()))
                   .toList();
                final Cookies requestCookies = Cookies.create();
                for (final var header: cookieHeaders) {
                    requestCookies.setAll(Cookie.from(header.value()));
                }
                requestBuilder.withCookies(requestCookies);

                // Find session
                Session session = sessionManager.find(requestBuilder);

                // Run validate hooks
                if (flow.shouldValidate()) {
                    final var validateHookContext = HttpValidateHookContext.builder()
                       .withSession(session)
                       .withFlowController(flow)
                       .withRequest(requestBuilder)
                       .withResponse(responseBuilder)
                       .build();
                    for (final HttpHook<HttpValidateHookContext> hook : hooks.byType(HttpValidateHookContext.class)) {
                        hook.run(validateHookContext);

                        if(flow.isInterrupted())
                            break;
                    }
                }

                // Execute all rewrites
                if (flow.shouldRewrite()) {
                    final HttpRewriterContext rewriterContext = HttpRewriterContextImpl.builder()
                       .withRequestBuilder(requestBuilder)
                       .withResponseBuilder(responseBuilder)
                       .build();
                    for (final HttpRewriter rewriter : rewriters) {
                        rewriter.modify(rewriterContext);
                    }
                }

                // All modification to request should be final,
                // Create request from builder
                final HttpRequest request = requestBuilder.build();
                errorBuilder.withRequest(request);

                // Populate with server defaults
                responseBuilder
                   .addHeader(HttpHeader.create("Server", "Demo Server"))
                   // TODO: minor, remove CEST, should be extract from date
                   .addHeader(HttpHeader.create("Date", date + " CEST"));
                if (responseBuilder.code() == 0)
                    responseBuilder.withResponseCode(200);

                // Only GET should attempt to automatically try to load resources,
                // this will be overridden by FlowController if set
                boolean shouldLoadResourceByDefault = HttpMethod.GET == requestBuilder.method();

                // Find matching endpoints and execute
                if (flow.shouldExecuteEndpoints()) {
                    final HttpEndPointContext endPointContext = HttpEndPointContext.builder()
                       .withRequest(request)
                       .withResponseBuilder(responseBuilder)
                       .withCookies(session.cookies())
                       .withSession(session)
                       .withFlowController(flow)
                       .withHooks(hooks)
                       .build();
                    final var matches = endPoints.match(request);
                    for (HttpEndPoint endPoint : matches) {
                        try {
                            endPoint.handle(endPointContext);
                        } catch (Throwable t) {
                            responseBuilder
                               .withDetails(t.getMessage())
                               .withResponseCode(500);
                            LOG.log(FINE, "Endpoint " + endPoint + " throw a exception", t);
                        }

                        if (flow.isInterrupted())
                            break;
                    }

                    if (matches.iterator().hasNext()) {
                        shouldLoadResourceByDefault = false;
                    }
                }

                // Set default value for loading of resource, if not already
                // set by FlowController
                if (flow.shouldLoadResource() == null) {
                    flow.setLoadResource(shouldLoadResourceByDefault);
                }

                // Check in case there was no match, then attempt to find default
                // resource loader. Otherwise, return 404
                if(flow.shouldLoadResource()) {
                    final var extension = request.extension() != null ?
                       request.extension() : "html";
                    final var type = RESOURCE_MAP.get(extension);
                    if (type != null) {
                        //final var path = request.path().endsWith("/") ?
                        //   request.path() + "index.html" :
                        final var resourceOpt = loader.load(type, request.path());
                        if (resourceOpt.isPresent()) {
                            responseBuilder.withPayload(resourceOpt.get());
                        }
                        else {
                            responseBuilder.withResponseCode(404)
                               .withPayload(DEFAULT_PAGE_404);
                        }
                    }
                }

                // Check if any endpoint determined the request
                // resource did not exists. Return 404.
                if (responseBuilder.code() == 404 && responseBuilder.hasNotPayload()) {
                    responseBuilder
                       .withPayload(DEFAULT_PAGE_404);
                }

                // Ask the cookie manager to determined which cookie should be
                // part of the reply.
                if (responseBuilder.code() >= 200 && responseBuilder.code() < 400) {
                    // Allow session manager to modify response
                    sessionManager.modified(session, request, responseBuilder);

                    // Convert all cookies to response headers
                    for (final var cookie: session.cookies()) {
                        responseBuilder
                           .addHeader("Set-Cookie", cookie.toString());
                    }
                }

                // Run post hooks
                if (flow.shouldPostProcessing()) {
                    final var postHookContext = HttpPostHookContext.builder()
                       .withSession(session)
                       .withRequest(request)
                       .withResponse(responseBuilder)
                       .build();
                    for (final HttpHook<HttpPostHookContext> hook: hooks.byType(HttpPostHookContext.class)) {
                        hook.run(postHookContext);
                    }
                }

                final var response = responseBuilder.build();
                errorBuilder.withResponse(response);
                LOG.log(Level.INFO, () -> "Respond with " + response.code());

                // Serialize and send response back to client
                serializer.serialize(connection.output(), response);

                // Reset error counter
                errorCounter = 0;
            }
            catch (Throwable e) {
                // Send exception to error handlers
                final var errorContext = errorBuilder.build();
                for (final var errorHandler: this.errorHandlers) {
                    errorHandler.onException(errorContext, e);
                }

                // Increase infinite error-loop fail-safe
                if (errorCounter < MAX_ERROR) {
                    errorCounter++;
                } else {
                    stop();
                }
            }
            finally {
                // Always close connection before attempting
                // to establish a new connection
                if ( connection != null && ! connection.keepAlive() ) {
                    connection.close();
                    connection = null;
                }
            }
        }
    }

    private HttpConnection accept() throws HttpServerException {
        final var builder = HttpConnection.builder();
        final Socket socket;

        try {
            socket = serverSocket.accept();
            builder.withSocket(socket);
        } catch (IOException e) {
            throw new HttpServerException("Unable to accept client", e);
        }

        try {
            builder.withOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new HttpServerException("Unable to open output stream to client", e);
        }

        try {
            builder.withInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new HttpServerException("Unable to open input stream to client", e);
        }

        return builder.build();
    }

    private static final HttpHyperText DEFAULT_PAGE_404 = HttpHyperText.create("""
    <html>
    <body>
        <h1>Page Not Found: 404</h1>
    </body>
    </html>
    """);
}
