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

class HttpServerImpl implements HttpServer {
    private static final int MAX_ERROR = 10;

    private final int port;
    private final int backlog;
    private final InetAddress addr;
    private final HttpParser parser;
    private final HttpSerializer serializer;
    private final HttpResourceLocator locator;
    private final String trustStoreName;
    private final char[] trustStorePassword;
    private final String keyStoreName;
    private final char[] keyStorePassword;
    private final String tlsVersion;
    
    private final HttpEndPoints endPoints = HttpEndPoints.create();
    private final HttpRewriters rewriters = HttpRewriters.create();
    private final HttpResourceLoader loader;
    private final boolean requireClientAuthentication;

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

    }

    private void run() throws HttpServerException {
        // In case a error-loop occur
        int errorCounter = 0;

        while (isRunning()) {
            try {
                System.out.println("Listening to client ...");
                final HttpConnection connection = accept();
                final HttpRequest.Builder requestBuilder = HttpRequestImpl.builder();
                final HttpResponse.Builder responseBuilder = HttpResponseImpl.builder();

                parser.parse(requestBuilder, connection.input());
                System.out.println("Request " + requestBuilder.method().name() + " " + requestBuilder.url());

                HttpRewriterContext rewriterContext = HttpRewriterContextImpl.builder()
                   .withRequestBuilder(requestBuilder)
                   .withResponseBuilder(responseBuilder)
                   .build();
                for(final HttpRewriter rewriter: rewriters) {
                    rewriter.modify(rewriterContext);
                }

                final HttpRequest request = requestBuilder.build();
                final var date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
                   .format(Calendar.getInstance().getTime());

                responseBuilder.withMethod(request.method())
                   .withUrl(request.url())
                   .withVersion(request.version())
                   .withResponseCode(200)
                   .addHeader(HttpHeader.create("Content-Type", "text/html"))
                   .addHeader(HttpHeader.create("Server", "Demo Server"))
                   .addHeader(HttpHeader.create("Date", date + " CET"));

                final HttpEndPointContext endPointContext = HttpEndPointContext.builder()
                   .withRequest(request)
                   .withResponseBuilder(responseBuilder)
                   .build();

                final var matches = endPoints.match(request);
                for(HttpEndPoint endPoint: matches) {
                    endPoint.handle(endPointContext);
                }

                if( ! matches.iterator().hasNext() ) {
                    responseBuilder.withResponseCode(404)
                       .withPayload(DEFAULT_PAGE_404);
                }
                else if (responseBuilder.code() == 404 && responseBuilder.hasNotPayload()) {
                    responseBuilder.withPayload(DEFAULT_PAGE_404);
                }

                final var response = responseBuilder.build();
                System.out.println("Respond with " + response.code());

                serializer.serialize(connection.output(), response);

                connection.close();

                // Reset error counter
                errorCounter = 0;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                if (errorCounter < MAX_ERROR) {
                    errorCounter++;
                } else {
                    stop();
                }
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

    private static final String DEFAULT_PAGE_404 = """
    <html>
    <body>
        <h1>Page Not Found: 404</h1>
    </body>
    </html>
    """;
}
