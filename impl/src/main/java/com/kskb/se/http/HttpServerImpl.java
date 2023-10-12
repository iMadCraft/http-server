package com.kskb.se.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

class HttpServerImpl implements HttpServer {
    private static final int MAX_ERROR = 10;
    private static final List<String> ROOT_INDEX_TARGETS = List.of("/", "/index.html", "/index.htm");

    private final HttpServerContext context;
    private final HttpParser parser;
    private final HttpSerializer serializer;

    private HttpServerState state = HttpServerState.INITIALIZED;
    private ServerSocket serverSocket;

    HttpServerImpl(HttpServerContext context) {
        this.context = context;
        this.parser = context.parser()
                .orElse(new HttpParserImpl());
        this.serializer = context.serializer()
                .orElse(new HttpSerializerImpl());
    }

    @Override
    public void start() throws HttpServerException {
        state = HttpServerState.STARTED;

        try {
            System.out.println("Starting http server listening to port " + context.port());
            serverSocket = new ServerSocket(context.port());
        } catch (IOException e) {
            throw new HttpServerException("Unable to start server", e);
        }

        state = HttpServerState.RUNNING;
        run();
    }

    @Override
    public void stop() throws HttpServerException {
        state = HttpServerState.STOP;
    }

    private void run() throws HttpServerException {
        // In case a error-loop occur
        int errorCounter = 0;

        while (isRunning()) {
            try {
                System.out.println("Listening to client ...");
                final HttpConnection connection = accept();
                System.out.println("Received request");
                final HttpRequest request = parser.parse(connection.input());

                final var date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
                        .format(Calendar.getInstance().getTime());
                final var builder = HttpResponseImpl.builder(request)
                        .withResponseCode(200)
                        .addHeader(HttpHeader.create("Content-Type", "text/html"))
                        .addHeader(HttpHeader.create("Server", "Demo Server"))
                        .addHeader(HttpHeader.create("Date", date + " CET"));

                if (ROOT_INDEX_TARGETS.contains(request.url())) {
                    final var payload = String.format("<html><body><h1>%s</h1><p>%s</p></body></html>", "Hello world", date);
                    builder.addHeader(HttpHeader.create("Content-Length", String.valueOf(payload.length())))
                            .withPayload(payload);
                }

                System.out.println("Send response");
                serializer.serialize(connection.output(), builder.build());
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
}
