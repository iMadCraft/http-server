package com.kskb.se.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface HttpEndPoints extends Iterable<HttpEndPoint> {
    void add(HttpMethod method, List<String> url, HttpEndPoint handler);

    static HttpEndPoints create() {
        return new HttpEndPointsImpl();
    }

    Iterable<HttpEndPoint> match(HttpRequest request);
}

class HttpEndPointsImpl implements HttpEndPoints {
    final List<EndPointEntry> entryList = new ArrayList<>();

    @Override
    public Iterable<HttpEndPoint> match(HttpRequest request) {
        List<HttpEndPoint> matches = new ArrayList<>();

        // Direct matching
        for (EndPointEntry entry: entryList) {
            final boolean condition =
               entry.method == request.method() &&
                  entry.url.contains(request.uri().getPath());
            if(condition)
                matches.add(entry.endPoint);
        }

        // Wildcard matching, if no direct matching found
        if (matches.isEmpty()) {
            for (EndPointEntry entry: entryList) {
                if (entry.method == request.method()) {
                    for (final String url: entry.url) {
                        // All match
                        if("*".equals(url)) {
                            if (entry.url.contains(request.uri().getPath()))
                                matches.add(entry.endPoint);
                        }
                        // Wildcard match
                        else if( url.endsWith("*") ) {
                            final String trimmedUrl = url.substring(0, url.length() - 2);
                            if(request.uri().getPath().startsWith(trimmedUrl))
                                matches.add(entry.endPoint);
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(matches);
    }

    @Override
    public void add(HttpMethod method, List<String> url, HttpEndPoint handler) {
        this.entryList.add(new EndPointEntry(method, Collections.unmodifiableList(url), handler));
    }

    @Override
    public Iterator<HttpEndPoint> iterator() {
        return entryList.stream()
           .map((e) -> e.endPoint)
           .iterator();
    }

    private record EndPointEntry(HttpMethod method, List<String> url, HttpEndPoint endPoint) {}
}