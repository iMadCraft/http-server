package com.kskb.se.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class HttpResponseImpl extends AbstractHttpPacket implements HttpResponse {
    private static final Map<Integer, String> RESPONSE_AS_TEXT;

    static {
        Map<Integer, String> temp = new HashMap<>();
        temp.put(100, "Continue");
        temp.put(101, "Switching Protocols");
        temp.put(102, "Processing (WebDAV)");
        temp.put(103, "Early Hints Experimental");
        temp.put(200, "OK");
        temp.put(201, "Created");
        temp.put(202, "Accepted");
        temp.put(203, "Non-Authoritative Information");
        temp.put(204, "No Content");
        temp.put(205, "Reset Content");
        temp.put(206, "Partial Content");
        temp.put(207, "Multi-Status (WebDAV)");
        temp.put(208, "Already Reported (WebDAV)");
        temp.put(226, "IM Used (HTTP Delta encoding)");
        temp.put(300, "Multiple Choices");
        temp.put(301, "Moved Permanently");
        temp.put(302, "Found");
        temp.put(303, "See Other");
        temp.put(304, "Not Modified");
        temp.put(305, "Use Proxy Deprecated");
        temp.put(306, "unused");
        temp.put(307, "Temporary Redirect");
        temp.put(308, "Permanent Redirect");
        temp.put(400, "Bad Request");
        temp.put(401, "Unauthorized");
        temp.put(402, "Payment Required Experimental");
        temp.put(403, "Forbidden");
        temp.put(404, "Not Found");
        temp.put(405, "Method Not Allowed");
        temp.put(406, "Not Acceptable");
        temp.put(407, "Proxy Authentication Required");
        temp.put(408, "Request Timeout");
        temp.put(409, "Conflict");
        temp.put(410, "Gone");
        temp.put(411, "Length Required");
        temp.put(412, "Precondition Failed");
        temp.put(413, "Payload Too Large");
        temp.put(414, "URI Too Long");
        temp.put(415, "Unsupported Media Type");
        temp.put(416, "Range Not Satisfiable");
        temp.put(417, "Expectation Failed");
        temp.put(418, "I'm a teapot");
        temp.put(421, "Misdirected Request");
        temp.put(422, "Unprocessable Content (WebDAV)");
        temp.put(423, "Locked (WebDAV)");
        temp.put(424, "Failed Dependency (WebDAV)");
        temp.put(425, "Too Early Experimental");
        temp.put(426, "Upgrade Required");
        temp.put(428, "Precondition Required");
        temp.put(429, "Too Many Requests");
        temp.put(431, "Request Header Fields Too Large");
        temp.put(451, "Unavailable For Legal Reasons");
        temp.put(500, "Internal Server Error");
        temp.put(501, "Not Implemented");
        temp.put(502, "Bad Gateway");
        temp.put(503, "Service Unavailable");
        temp.put(504, "Gateway Timeout");
        temp.put(505, "HTTP Version Not Supported");
        temp.put(506, "Variant Also Negotiates");
        temp.put(507, "Insufficient Storage (WebDAV)");
        temp.put(508, "Loop Detected (WebDAV)");
        temp.put(510, "Not Extended");
        temp.put(511, "Network Authentication Required");
        RESPONSE_AS_TEXT = Collections.unmodifiableMap(temp);
    }

    private final int code;
    private final String details;

    private HttpResponseImpl(Builder builder) {
        super(builder);
        this.code = builder.code;
        this.details = builder.details;
    }

    static HttpResponse.Builder builder() {
        return new Builder();
    }

    @Override
    public int code() {
        return this.code;
    }

    @Override
    public String codeAsText() {
        // TODO: major, add more!
        final var text = RESPONSE_AS_TEXT.get(this.code);
        return text != null ? text : "Unknown";
    }

    @Override
    public String details() {
        return this.details;
    }

    private static class Builder extends AbstractHttpPacket.Builder<HttpResponse.Builder> implements HttpResponse.Builder {


        private int code;
        private String details;

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public HttpResponse.Builder withResponseCode(int code) {
            this.code = code;
            return this;
        }

        @Override
        public HttpResponse.Builder withDetails(String message) {
            this.details = message;
            return this;
        }

        @Override
        public HttpResponse build() {
            if (payload().isPresent()) {
                final var payload = payload().get();
                addHeader(HttpHeader.create("Content-Length", String.valueOf(payload.size())));
                addHeader(HttpHeader.create("Content-Type", payload.contentType()));
            }
            else {
                addHeader(HttpHeader.create("Content-Length", "0"));
            }
            return new HttpResponseImpl(this);
        }
    }
}
