package com.kskb.se.http;

import java.util.*;
import java.util.stream.Collectors;

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
        for (EndPointEntry entry: entryList) {
            final boolean condition =
               entry.method == request.method() &&
                  entry.url.contains(request.url());
            if(condition)
                matches.add(entry.endPoint);
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