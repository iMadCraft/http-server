package com.kskb.se.http;

import java.net.InetAddress;
import java.util.Optional;

public interface HttpServerContext {
    static Builder builder() {
        return HttpServerContextImpl.builder();
    }

    int port();
    int backlog();
    InetAddress addr();

    // SSL
    String trustStoreName();
    char[] trustStorePassword();
    String keyStoreName();
    char[] keyStorePassword();
    String tlsVersion();

    // SSL extra
    boolean requireClientAuthentication();

    // Helpers
    HttpResourceLocator locator();
    HttpResourceLoader loader();
    HttpErrorHandlers errorHandlers();
    SessionManager sessionManager();

    Optional<HttpParser> parser();
    Optional<HttpSerializer> serializer();


    interface Builder {
        Builder withPort(int port);

        Builder withTrustStore(String path);
        Builder withTrustStorePassword(char[] password);
        Builder withKeyStore(String path);
        Builder withKeyStorePassword(char[] password);

        Builder withLocator(HttpResourceLocator resourceLocator);
        Builder addErrorHandler(HttpErrorHandler testHttpServer);
        Builder withSessionManager(SessionManager sessionManager);

        HttpServerContext build();

    }
}

class HttpServerContextImpl implements HttpServerContext {
    private final int port;
    private final int backlog = 100;
    private final InetAddress addr = null;

    private final String trustStoreName;
    private final char[] trustStorePassword;
    private final String keyStoreName;
    private final char[] keyStorePassword;
    private final String tlsVersion = "TLSv1.2";

    private final HttpParser parser;
    private final HttpSerializer serializer;
    private final HttpResourceLocator locator;
    private final HttpResourceLoader loader;
    private final HttpErrorHandlers errorHandlers;
    private final SessionManager sessionManager;

    private HttpServerContextImpl(Builder builder) {
        this.port = builder.port;
        this.trustStoreName = builder.trustStoreName;
        this.trustStorePassword = builder.trustStorePassword;
        this.keyStoreName = builder.keyStoreName;
        this.keyStorePassword = builder.keyStorePassword;
        this.parser = builder.parser;
        this.serializer = builder.serializer;
        this.locator = builder.locator;
        this.loader = builder.loader;
        this.errorHandlers = builder.errorHandlers;
        this.sessionManager = builder.sessionManager;
    }

    static Builder builder() {
        return new Builder();
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public int backlog() {
        return this.backlog;
    }

    @Override
    public InetAddress addr() {
        return this.addr;
    }

    @Override
    public String trustStoreName() {
        return this.trustStoreName;
    }

    @Override
    public char[] trustStorePassword() {
        return this.trustStorePassword;
    }

    @Override
    public String keyStoreName() {
        return this.keyStoreName;
    }

    @Override
    public char[] keyStorePassword() {
        return this.keyStorePassword;
    }

    @Override
    public String tlsVersion() {
        return this.tlsVersion;
    }

    @Override
    public boolean requireClientAuthentication() {
        return false;
    }

    @Override
    public Optional<HttpParser> parser() {
        return Optional.ofNullable(this.parser);
    }

    @Override
    public Optional<HttpSerializer> serializer() {
        return Optional.ofNullable(this.serializer);
    }

    @Override
    public HttpResourceLoader loader() {
        return this.loader;
    }

    @Override
    public HttpResourceLocator locator() {
        return this.locator;
    }

    @Override
    public HttpErrorHandlers errorHandlers() {
        return this.errorHandlers;
    }

    @Override
    public SessionManager sessionManager() {
        return this.sessionManager;
    }

    static class Builder implements HttpServerContext.Builder {
        private int port = HttpServer.DEFAULT_PORT;

        private String trustStoreName;
        private char[] trustStorePassword;
        private String keyStoreName;
        private char[] keyStorePassword;

        private HttpParser parser;
        private HttpSerializer serializer;
        private HttpResourceLocator locator;
        private HttpResourceLoader loader;
        private final HttpErrorHandlers errorHandlers = HttpErrorHandlers.create();
        private SessionManager sessionManager;

        @Override
        public HttpServerContext.Builder withPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public HttpServerContext.Builder withTrustStore(String path) {
            this.trustStoreName = path;
            return this;
        }

        @Override
        public HttpServerContext.Builder withTrustStorePassword(char[] password) {
            this.trustStorePassword = password;
            return this;
        }

        @Override
        public HttpServerContext.Builder withKeyStore(String path) {
            this.keyStoreName = path;
            return this;
        }

        @Override
        public HttpServerContext.Builder withKeyStorePassword(char[] password) {
            this.keyStorePassword = password;
            return this;
        }

        @Override
        public HttpServerContext.Builder withLocator(HttpResourceLocator resourceLocator) {
            this.locator = resourceLocator;
            return this;
        }

        @Override
        public HttpServerContext.Builder addErrorHandler(HttpErrorHandler errorHandler) {
            this.errorHandlers.add(errorHandler);
            return this;
        }

        @Override
        public HttpServerContext.Builder withSessionManager(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
            return this;
        }

        @Override
        public HttpServerContext build() {
            if (this.errorHandlers.isEmpty())
                this.errorHandlers.add(new HttpConsoleLogger());
            this.locator = this.locator != null ? this.locator :
               HttpResourceLocator.builder().withDefaults("impl").build();
            this.loader = this.loader != null ? this.loader :
               HttpResourceLoader.create(this.locator);
            return new HttpServerContextImpl(this);
        }
    }
}
