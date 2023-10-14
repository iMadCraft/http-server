package com.kskb.se.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

class HttpParserImpl implements HttpParser {
    private static final List<String> SUPPORTED_VERSION = List.of("1.0", "1.1", "1.2", "1.3");

    @Override
    public void parse(HttpRequest.Builder builder, InputStream inputStream) throws HttpServerException {
        var input = new BufferedReader(new InputStreamReader(inputStream));

        // NOTE: Not the best parse, The HTTP standard specifies 0x0D, 0x0A
        // as line termination. This solution handle more line terminations
        // scenarios, not supported by HTTP. This will do for this demo.
        try {
            final var firstLine = input.readLine();
            final var firstLineParts = firstLine.split(" ");

            if (firstLineParts.length != 3)
                throw new HttpServerException("Client request did not contain valid initial line");

            // Parse HTTP method
            final var methodAsString = firstLineParts[0];
            final var method = HttpMethod.softValueOf(methodAsString);
            if (method == null)
                throw new HttpServerException("Unsupported http method " + method);
            builder.withMethod(method);

            // Parse HTTP URL
            final var urlAsString = firstLineParts[1];
            builder.withUrl(urlAsString);

            // Parse HTTP Version
            final var versionAsString = firstLineParts[2];
            final String[] versionParts = versionAsString.split("/");
            if (!(versionParts.length == 2 && "HTTP".equals(versionParts[0]) && SUPPORTED_VERSION.contains(versionParts[1])))
                throw new HttpServerException("Unsupported http version " + versionAsString);
            builder.withVersion(versionParts[1]);

            // Parse HTTP Headers
            var line = input.readLine();
            while (line != null && !line.isEmpty()) {
                var headerParts = line.split(": ");

                if (headerParts.length == 2 && !headerParts[0].isEmpty() && !headerParts[1].isEmpty()) {
                    builder.addHeader(HttpHeader.create(headerParts[0], headerParts[1]));
                } else {
                    System.out.println("Not parsable header: " + line);
                }

                line = input.readLine();
            }
        } catch (IOException e) {
            throw new HttpServerException("Unable to parse client request", e);
        }
    }
}
