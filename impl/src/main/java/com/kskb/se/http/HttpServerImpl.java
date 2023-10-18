package com.kskb.se.http;

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

import static com.kskb.se.http.HttpMethod.GET;

class HttpServerImpl implements HttpServer {
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
        this.parser = context.parser()
           .orElse(new HttpParserImpl());
        this.serializer = context.serializer()
           .orElse(new HttpSerializerImpl());
    }

    public ServerSocket createEncryptedServerSocket() throws Exception {
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

    public ServerSocket createUnencrypedServerSocket() throws IOException {
        return new ServerSocket(port, backlog, addr);
    }

    @Override
    public void start() throws HttpServerException {
        state = HttpServerState.STARTED;

        try {
            if (trustStoreName != null && ! trustStoreName.isEmpty()) {
                System.out.println("Starting https server listening to port " + port);
                serverSocket = createEncryptedServerSocket();
            }
            else {
                System.out.println("Starting http server listening to port " + port);
                serverSocket = createUnencrypedServerSocket();
            }
        } catch (Exception e) {
            throw new HttpServerException("Unable to start server", e);
        }

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
        return loader;
    }

    private void sanitize() {
        this.trustStorePassword = null;
        this.keyStorePassword = null;
    }

    private void run() throws HttpServerException {
        // Used for a simple infinite error-loop prevention,
        // reset after a successful attempt
        int errorCounter = 0;

        // Main Loop. Following flow:
        //    * Accept client request
        //    * Parse request
        //    * Modify request (rewriters)
        //    * Create response builder with server defaults
        //    * Find matching endpoints and forward request and responseBuilder
        while (isRunning()) {
            HttpConnection connection = null;
            try {
                System.out.println("Listening to client ...");

                // Blocking call. Wait until a client socket has been accepted
                connection = accept();

                final var requestBuilder = HttpRequestImpl.builder();
                final var responseBuilder = HttpResponseImpl.builder();
                final var date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
                   .format(Calendar.getInstance().getTime());

                // Parse incoming stream and populate request builder
                if(parser.parse(requestBuilder, connection.input())) {
                    System.out.println("Request " +
                       requestBuilder.method().name() + " " + requestBuilder.uri());
                }
                else {
                    // Error is handled by parser, just
                    // continue with the next request
                    continue;
                }

                // Execute all rewrites
                HttpRewriterContext rewriterContext = HttpRewriterContextImpl.builder()
                   .withRequestBuilder(requestBuilder)
                   .withResponseBuilder(responseBuilder)
                   .build();
                for(final HttpRewriter rewriter: rewriters) {
                    rewriter.modify(rewriterContext);
                }

                // All modification to request should be final,
                // Create request from builder
                final HttpRequest request = requestBuilder.build();

                // Populate with server defaults
                responseBuilder
                   .withResponseCode(200)
                   .addHeader(HttpHeader.create("Server", "Demo Server"))
                   .addHeader(HttpHeader.create("Date", date + " CET"));

                // Find matching endpoints and execute
                final HttpEndPointContext endPointContext = HttpEndPointContext.builder()
                   .withRequest(request)
                   .withResponseBuilder(responseBuilder)
                   .build();
                final var matches = endPoints.match(request);
                for(HttpEndPoint endPoint: matches) {
                    try {
                        endPoint.handle(endPointContext);
                    }
                    catch (Throwable t) {
                        responseBuilder
                           .withDetails(t.getMessage())
                           .withResponseCode(500);
                    }
                }

                // Check in case there was no match, then attempt to find default
                // resource loader. Otherwise, return 404
                if( ! matches.iterator().hasNext() ) {
                    final var type = RESOURCE_MAP.get(request.extension());
                    if (type != null) {
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
                else if (responseBuilder.code() == 404 && responseBuilder.hasNotPayload()) {
                    responseBuilder
                       .withPayload(DEFAULT_PAGE_404);
                }

                final var response = responseBuilder.build();
                System.out.println("Respond with " + response.code());

                // Serialize and send response back to client
                serializer.serialize(connection.output(), response);

                // Reset error counter
                errorCounter = 0;
            }
            catch (HttpServerException e) {
                System.err.println(e.getMessage());
                if (e.getCause() != null) {
                    e.getCause().printStackTrace();
                }
                if (errorCounter < MAX_ERROR) {
                    errorCounter++;
                } else {
                    stop();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (errorCounter < MAX_ERROR) {
                    errorCounter++;
                } else {
                    stop();
                }
            }
            finally {
                if (connection != null) {
                    connection.close();
                }
                connection = null;
            }
        }
    }

    private boolean isRunning() {
        return state == HttpServerState.RUNNING;
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
