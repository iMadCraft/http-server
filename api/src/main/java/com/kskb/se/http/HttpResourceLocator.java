package com.kskb.se.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kskb.se.http.HttpResourceLocation.*;

public interface HttpResourceLocator {
    Iterable<HttpResourceLocationEntry> locations();

    static Builder builder() {
        return HttpResourceLocatorImpl.builder();
    }

    Iterable<String> getCandidates(HttpResourceLocation location, String name);

    interface Builder {
        Builder withDefaults(String projectName);
        Builder addLocation(HttpResourceLocation location, String path);
        HttpResourceLocator build();
    }
}

class HttpResourceLocatorImpl implements HttpResourceLocator {
    private final List<HttpResourceLocationEntry> locations;

    private HttpResourceLocatorImpl(Builder builder) {
        this.locations = Collections.unmodifiableList(builder.locations);
    }

    @Override
    public Iterable<HttpResourceLocationEntry> locations() {
        return locations;
    }

    @Override
    public Iterable<String> getCandidates(HttpResourceLocation location, String name) {
        final ArrayList<String> locations = new ArrayList<>();
        for (final var entry: this.locations) {
            if (entry.location() == location) {
                locations.add(entry.path() + "/" + name);
            }
        }
        return Collections.unmodifiableList(locations);
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder implements HttpResourceLocator.Builder {
        private final List<HttpResourceLocationEntry> locations = new ArrayList<>();

        @Override
        public Builder withDefaults(String projectName) {
            addLocation(HTML, "resource:///htdocs");
            addLocation(HTML, projectName + "/src/main/html");
            addLocation(CSS, "resource:///htdocs");
            addLocation(CSS, projectName + "/src/main");
            addLocation(HTDOCS, "resource:///htdocs");
            addLocation(SECRET, "resource:///secret");
            addLocation(SECRET, projectName + "/src/main/resources/secret");
            return this;
        }

        @Override
        public Builder addLocation(HttpResourceLocation location, String path) {
            locations.add(new HttpResourceLocationEntry(location, path));
            return this;
        }

        @Override
        public HttpResourceLocator build() {
            return new HttpResourceLocatorImpl(this);
        }
    }
}
