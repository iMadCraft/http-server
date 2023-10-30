package com.kskb.se.http;

import java.util.*;
import java.util.stream.Stream;

public interface HttpHeaders extends Iterable<HttpHeader> {
    long size();
    Optional<String> get(String name);
    Stream<HttpHeader> stream();

    static HttpHeaders create(List<HttpHeader> list) {
        return new HttpHeadersImpl(list);
    }

}

class HttpHeadersImpl implements HttpHeaders {
    private final List<HttpHeader> list;

    HttpHeadersImpl(List<HttpHeader> list) { this.list = list; }

    @Override
    public long size() {
        return list.size();
    }

    @Override
    public Iterator<HttpHeader> iterator() { return list.iterator(); }

    @Override
    public Optional<String> get(String name) {
        String result = null;
        for (final var header: list) {
            if(Objects.equals(header.name(), name)) {
                result = header.value();
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Stream<HttpHeader> stream() {
        return list.stream();
    }
}
