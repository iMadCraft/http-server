package com.kskb.se.http;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

class HttpParserImpl implements HttpParser {
    private static final List<String> SUPPORTED_VERSION = List.of("1.0", "1.1", "1.2", "1.3");

    @Override
    public boolean parse(HttpRequest.Builder builder, InputStream inputStream) throws HttpServerException {
        final var requestBuilder = HttpBufferedReader.create(builder, inputStream)
           .read()
               .onError(Throwable.class, Throwable::printStackTrace)
               .onError(String.class, System.out::println)
               .onError(UnknownRequest.class, System.out::println)
                   .orElse(null);
        return  requestBuilder != null;
    }
}

interface HttpBufferedReader {
    static HttpBufferedReader create(HttpRequest.Builder builder, InputStream stream) {
        return new HttpBufferedReaderImpl(builder, stream);
    }

    Result<HttpRequest.Builder, Object> read();
}

class HttpBufferedReaderImpl implements HttpBufferedReader {
    private final InputStream stream;
    private final HttpRequest.Builder builder;

    private final int capacity = 4096 * 4096; // Max: 16MB request
    private final byte[] buf = new byte[capacity];

    private int index = 0;

    HttpBufferedReaderImpl(HttpRequest.Builder builder, InputStream stream) {
        this.builder = builder;
        this.stream = stream;
    }

    public Result<HttpRequest.Builder, Object> read() {
        final int SPACE = 0x20, LINE_FEED = 0x0A, CARRIER_RETURN = 0x0D;

        final int size;
        try {
            size = this.stream.read(buf, 0, capacity);
        } catch (IOException e) {
            // This can occur with self-signed certificates
            // that is not installed on client's system.
            // FollowUp request seems to work
            if (e instanceof SSLHandshakeException && e.getMessage().contains("certificate_unknown"))
                return Result.error("Request resulted in faulty handshake");
            else if (e instanceof SSLException) {
                if ("Broken pipe".equals(e.getMessage()))
                    return Result.error("Request resulted in broken pipe");
                else if ("Unsupported or unrecognized SSL message".equals(e.getMessage()))
                    return Result.error(e.getMessage());
                else
                    return Result.error(e);
            }
            else
                return Result.error(e);
        }

        int i;
        // section-block: Read method
        {
            if (buf[0] == 'G' &&
               buf[1] == 'E' &&
               buf[2] == 'T' &&
               buf[3] == SPACE) {
                builder.withMethod(HttpMethod.GET);
                index += 4;
            } else if (buf[0] == 'P' &&
               buf[1] == 'O' &&
               buf[2] == 'S' &&
               buf[3] == 'T' &&
               buf[4] == SPACE) {
                builder.withMethod(HttpMethod.POST);
                index += 5;
            } else if (buf[0] == 'P' &&
               buf[1] == 'U' &&
               buf[2] == 'T' &&
               buf[3] == SPACE) {
                builder.withMethod(HttpMethod.PUT);
                index += 5;
            } else if (buf[0] == 'D' &&
               buf[1] == 'E' &&
               buf[2] == 'L' &&
               buf[3] == 'E' &&
               buf[4] == 'T' &&
               buf[5] == 'E' &&
               buf[6] == SPACE) {
                builder.withMethod(HttpMethod.DELETE);
                index += 7;
            } else {
                return Result.error(UnknownRequest.create(buf));
            }
        }

        // section-block: Read url
        {
            i = index;
            while ( i < size && buf[i] != SPACE ) i++;
            if (buf[i] == SPACE) {
                builder.withUrl(new String(buf, index, i - index, StandardCharsets.US_ASCII));
                index = i;
            }
            else {
                return Result.error(UnknownRequest.create(buf));
            }
        }

        // section-block: Read version
        {
            i = index;
            while ( i < (size + 1) && buf[i] != CARRIER_RETURN && buf[i+1] != LINE_FEED ) i++;
            if (buf[i] == CARRIER_RETURN) {
                builder.withVersion(new String(buf, index, i - index, StandardCharsets.US_ASCII));
                index = i + 2;
            }
            else {
                return Result.error(UnknownRequest.create(buf));
            }
        }

        // section-block: Read all headers
        {
            i = index;
            String name, value;
            while (i < (size + 1) && buf[i] != CARRIER_RETURN && buf[i + 1] != LINE_FEED) {
                while (i < size && buf[i] != SPACE) i++;
                if (buf[i] == SPACE) {
                    if (buf[i - 1] == ':') {
                        name = new String(buf, index, i - index - 1, StandardCharsets.US_ASCII);
                    }
                    else {
                        name = new String(buf, index, i - index, StandardCharsets.US_ASCII);
                    }
                    index = i + 1;
                } else {
                    return Result.error(UnknownRequest.create(buf));
                }

                while (i < (size + 1) && buf[i] != CARRIER_RETURN && buf[i + 1] != LINE_FEED) i++;
                if (buf[i] == CARRIER_RETURN) {
                    value = new String(buf, index, i - index, StandardCharsets.US_ASCII);
                    builder.addHeader(HttpHeader.create(name, value));
                    index = i + 2;
                } else {
                    return Result.error(UnknownRequest.create(buf));
                }
                i += 2;
            }
            index = i + 2;
        }

        if (index < size) {
            System.out.println("Warning request payload (" + (size - index) + ") is ignored");
        }

        return Result.of(builder);
    }
}
