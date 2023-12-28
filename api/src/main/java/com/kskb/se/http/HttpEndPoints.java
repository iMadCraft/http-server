package com.kskb.se.http;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kskb.se.base.Strings.join;
import static com.kskb.se.base.Strings.substring;

public interface HttpEndPoints extends Iterable<HttpEndPoint> {
    void add(HttpMethod method, List<Pattern> url, HttpEndPoint handler);

    static HttpEndPoints create() {
        return new HttpEndPointsImpl();
    }

    Iterable<Map.Entry<Matcher, HttpEndPoint>> match(HttpRequest request);
}

class HttpEndPointsImpl implements HttpEndPoints {
    final List<EndPointEntry> entryList = new ArrayList<>();

    @Override
    public Iterable<Map.Entry<Matcher, HttpEndPoint>> match(HttpRequest request) {
        Map<Matcher, HttpEndPoint> matches = new HashMap<>();

        // Direct matching
        for (EndPointEntry entry: entryList) {
            if(entry.method == request.method()) {
                for (final var pattern: entry.url) {
                    final var matcher = pattern.matcher(request.path());
                    if(matcher.matches()) {
                        matches.put(matcher, entry.endPoint);
                    }
                }
            }
        }

        return matches.entrySet();
    }

    @Override
    public void add(HttpMethod method, List<Pattern> url, HttpEndPoint handler) {
        this.entryList.add(new EndPointEntry(method, Collections.unmodifiableList(url), handler));
    }

    @Override
    public Iterator<HttpEndPoint> iterator() {
        return entryList.stream()
           .map((e) -> e.endPoint)
           .iterator();
    }

    private record EndPointEntry(HttpMethod method, List<Pattern> url, HttpEndPoint endPoint) {}
}
