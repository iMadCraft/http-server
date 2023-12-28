package com.kskb.se.http;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class HttpParserImpl implements HttpParser {

    @Override
    public boolean parse(HttpRequest.Builder builder, InputStream inputStream) {
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
    private static final int BLOCK_SIZE = 4096;
    private static final int CAPACITY = 4096 * 4096; // Max: 16MB request

    private final InputStream stream;
    private final HttpRequest.Builder builder;

    private final byte[] buf = new byte[CAPACITY];

    private int index = 0;

    HttpBufferedReaderImpl(HttpRequest.Builder builder, InputStream stream) {
        this.builder = builder;
        this.stream = stream;
    }

    public Result<HttpRequest.Builder, Object> read() {
        final int SPACE = 0x20, LINE_FEED = 0x0A, CARRIER_RETURN = 0x0D;

        final int size;
        try {
            // final int availableBytes = stream.available();
            // if (availableBytes <= 0) {
            //     builder
            //        .withMethod(HttpMethod.CONNECT)
            //        .withUri(URI.create("localhost:8081"))
            //        .withVersion("HTTP/1.1")
            //        .addHeader(HttpHeader.create("Host", "localhost:8081"));
            //    return Result.of(builder);
            // }
            // else
            //     System.out.println("Available bytes " + availableBytes);
            // Sometimes a stream request with a no end terminations is sent.
            // Read the first block and probe result before continuing
            // reading the rest.

            int rb = stream.read(buf, 0, BLOCK_SIZE);
            if (rb == -1) {
                return Result.error("Unable to read from stream");
            }
            else if (rb == BLOCK_SIZE) {
                boolean valid =
                    ( buf[0] == 'G' && buf[1] == 'E' && buf[2] == 'T' ) ||
                    ( buf[0] == 'P' && buf[1] == 'O' && buf[2] == 'S' ) ||
                    ( buf[0] == 'P' && buf[1] == 'U' && buf[2] == 'T' ) ||
                    ( buf[0] == 'D' && buf[1] == 'E' && buf[2] == 'L' ) ||
                    ( buf[0] == 'H' && buf[1] == 'E' && buf[2] == 'A' ) ||
                    ( buf[0] == 'P' && buf[1] == 'A' && buf[2] == 'T' ) ||
                    ( buf[0] == 'C' && buf[1] == 'O' && buf[2] == 'N' ) ||
                    ( buf[0] == 'T' && buf[1] == 'R' && buf[2] == 'A' ) ||
                    ( buf[0] == 'O' && buf[1] == 'P' && buf[2] == 'T' );

                // Continue read the rest if valid request
                if (valid) {
                    System.out.println("Sample: " + new String(buf, 0, 256, StandardCharsets.UTF_8));
                    rb += this.stream.read(buf, BLOCK_SIZE, CAPACITY - BLOCK_SIZE);
                }
                else
                    return Result.error(UnknownRequest.create(Arrays.copyOf(buf, rb)));
            }
            size = rb;
        } catch (IOException e) {
            // This can occur with self-signed certificates
            // that is not installed on client's system.
            // FollowUp request seems to work
            if (e instanceof SSLHandshakeException && e.getMessage().contains("certificate_unknown"))
                return Result.error("Request resulted in faulty handshake");
            else if (e instanceof SocketException && "Broken pipe".equals(e.getMessage()))
                return Result.error("Request resulted in broken pipe");
            else if (e instanceof SSLException && "Unsupported or unrecognized SSL message".equals(e.getMessage()))
                return Result.error(e.getMessage());
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
                index += 4;
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
                return Result.error(UnknownRequest.create(Arrays.copyOf(buf, size)));
            }
        }

        // section-block: Read url
        final String urlPath;
        // TODO: critical, replace hard-coded uri
        String urlHost = "localhost";
        String urlPort = "80";
        {
            i = index;
            while ( i < size && buf[i] != SPACE ) i++;
            if (buf[i] == SPACE) {
                 urlPath = new String(buf, index, i - index, StandardCharsets.US_ASCII);
                 index = i;
            }
            else {
                return Result.error(UnknownRequest.create(Arrays.copyOf(buf, size)));
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
                return Result.error(UnknownRequest.create(Arrays.copyOf(buf, size)));
            }
        }

        // section-block: Read all headers
        {
            i = index;
            String name, value;
            while ( (i + 1) < size && buf[i] != CARRIER_RETURN && buf[i + 1] != LINE_FEED) {
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
                    return Result.error(UnknownRequest.create(Arrays.copyOf(buf, size)));
                }

                while (i < (size + 1) && buf[i] != CARRIER_RETURN && buf[i + 1] != LINE_FEED) i++;
                if (buf[i] == CARRIER_RETURN) {
                    value = new String(buf, index, i - index, StandardCharsets.US_ASCII);
                    final var header = HttpHeader.create(name, value);
                    builder.addHeader(header);

                    // Extra Header Logic
                    if("Host".equals(name)) {
                        final var portStartIndex = value.indexOf(":");
                        final var firstSlashIndex = value.indexOf("/");
                        final var portEndIndex = firstSlashIndex >= 0 ? 
                           firstSlashIndex : value.length();
                        if (portStartIndex >= 0) {
                           urlHost = value.substring(0, portStartIndex);
                           urlPort = value.substring(portStartIndex, portEndIndex);                        
                        }
                        else {
                           urlHost = value.substring(0, portStartIndex);
                           urlPort = value.substring(portEndIndex);                        
                        }
                    }

                    index = i + 2;
                } else {
                    return Result.error(UnknownRequest.create(Arrays.copyOf(buf, size)));
                }
                i += 2;
            }
            index = i + 2;
        }

        final String slashedUrlPath = urlPath.startsWith("/") ?
           urlPath : "/" + urlPath;
        final String commaedUrlPort = urlPort.startsWith(":") ?
           urlPort : ":" + urlPort;

        URI uri = URI.create("https://" + urlHost + commaedUrlPort + slashedUrlPath);
        System.out.println("Request URI: " + uri.toString());
        builder.withUri(uri);

        if (index < size) {
            final HttpHeader contentLength = builder.headers().stream()
               .filter(e -> "content-length".equalsIgnoreCase(e.name()))
                  .findFirst()
                     .orElse(null);
            final HttpHeader contentType = builder.headers().stream()
               .filter(e -> "content-type".equalsIgnoreCase(e.name()))
                  .findFirst()
                     .orElse(null);
            final int payloadSize = contentLength != null && contentLength.value() != null ?
               Integer.parseInt(contentLength.value()) : 0;
            final int dataLeft = size - index;
            if (payloadSize >= dataLeft && payloadSize <= dataLeft) {
                builder.withPayload(HttpPlainText.create(new String(buf, index, payloadSize, StandardCharsets.UTF_8)));
            }
        }

        return Result.of(builder);
    }
}
