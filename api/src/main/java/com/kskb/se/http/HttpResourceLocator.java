package com.kskb.se.http;

import com.kskb.se.base.Nullable;

import java.net.URI;
import java.util.*;

import static com.kskb.se.http.HttpResourceLocation.*;

public interface HttpResourceLocator {
    Iterable<HttpResourceLocationEntry> locations();

    static Builder builder() {
        return HttpResourceLocatorImpl.builder();
    }

    Iterable<String> getCandidates(HttpResourceLocation location, String name);

    interface Builder {
        Builder withDefaults();
        Builder withDefaults(@Nullable String subProjectName);
        Builder addLocation(HttpResourceLocation location, String path);
        Builder addLocationFromEnv(String envName);
        Builder addRemap(String originalPath, String remappedPath);
        Builder addRemapFromEnv(String envName);
        Builder addExternal(String resource, URI uri);
        Builder addExternalFromEnv(String envName);
        HttpResourceLocator build();


    }
}

class HttpResourceLocatorImpl implements HttpResourceLocator {
    private final List<HttpResourceLocationEntry> locations;
    private final Map<String, String> remapper;
    private final Map<String, URI> externals;

    private HttpResourceLocatorImpl(Builder builder) {
        this.locations = Collections.unmodifiableList(builder.locations);
        this.remapper = Collections.unmodifiableMap(builder.remapper);
        this.externals = Collections.unmodifiableMap(builder.externals);
    }

    @Override
    public Iterable<HttpResourceLocationEntry> locations() {
        return locations;
    }

    @Override
    public Iterable<String> getCandidates(HttpResourceLocation location, String name) {
        final ArrayList<String> locations = new ArrayList<>();
        populateCandidates(location, name, locations);
        return Collections.unmodifiableList(locations);
    }

    private void populateCandidates(HttpResourceLocation location, String name, List<String> locations) {
        // First add all possible location for original resource
        for (final var entry: this.locations) {
            if (entry.location() == location) {
                locations.add(entry.path() + "/" + name);
            }
        }

        // Then add remapped resource with all its possible locations
        for (final var remap: remapper.entrySet()) {
            final var remappedPath = remap.getValue();
            if (Objects.equals(remap.getKey(), name)) {
                populateCandidates(location, remappedPath, locations);
            }
        }

        // Lastly check if resource has external mapping
        for (final var external: externals.entrySet()) {
            final URI externalPath = external.getValue();
            if (Objects.equals(external.getKey(), name)) {
                locations.add(externalPath.toString());
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder implements HttpResourceLocator.Builder {
        private final List<HttpResourceLocationEntry> locations = new ArrayList<>();
        private final Map<String, String> remapper = new HashMap<>();
        private final HashMap<String, URI> externals = new HashMap<>();

        @Override
        public HttpResourceLocator.Builder withDefaults() {
            return this.withDefaults(null);
        }

        @Override
        public Builder withDefaults(final String subProjectName) {
            addLocation(HTML, "resource:///htdocs");
            addLocation(HTML, "src/main/html");
            if (subProjectName != null)
                addLocation(HTML, subProjectName + "/src/main/html");
            addLocation(CSS, "resource:///htdocs");
            addLocation(CSS, "src/main/css");
            if (subProjectName != null) {
                addLocation(CSS, "src/main/css/" + subProjectName);
                addLocation(CSS, subProjectName + "/src/main");
            }
            addLocation(JAVASCRIPT, "resource:///htdocs");
            if (subProjectName != null) {
                addLocation(JAVASCRIPT, subProjectName + "/src/3pp");
                addLocation(JAVASCRIPT, subProjectName + "/src/main");
            }
            addLocation(HTDOCS, "resource:///htdocs");
            addLocation(ICO, "resource:///htdocs");
            addLocation(ICO, "src/main/resources");
            addLocation(ICO, "impl/src/main/resources");
            addLocation(SECRET, "resource:///secret");
            addLocation(SECRET, "src/main/resources/secret");
            if (subProjectName != null)
                addLocation(SECRET, subProjectName + "/src/main/resources/secret");
            return this;
        }

        @Override
        public Builder addLocationFromEnv(String envName) {
            final String values = System.getenv(envName);
            if (values != null) {
                for(final var part : values.split(":")) {
                    final var pair = part.split("=");
                    if (pair.length != 2)
                        continue;
                    for (final var type: HttpResourceLocation.values()) {
                        if(type.name().equals(pair[0])) {
                            System.out.printf("Add location %.4096s=%.4096s from envvars%n", pair[0], pair[1]);
                            addLocation(type, pair[1]);
                        }
                    }
                }
            }
            return this;
        }

        @Override
        public Builder addLocation(HttpResourceLocation location, String path) {
            locations.add(new HttpResourceLocationEntry(location, path));
            return this;
        }

        @Override
        public HttpResourceLocator.Builder addRemap(String originalPath, String remappedPath) {
            remapper.put(originalPath, remappedPath);
            return this;
        }

        @Override
        public HttpResourceLocator.Builder addRemapFromEnv(String envName) {
            final String values = System.getenv(envName);
            if (values != null) {
                for(final var part : values.split(":")) {
                    final var pair = part.split("=");
                    if (pair.length != 2)
                        continue;
                    System.out.printf("Add remapping %.4096s=%.4096s from envvars%n", pair[0], pair[1]);
                    addRemap(pair[0], pair[1]);
                }
            }
            return this;
        }

        @Override
        public HttpResourceLocator.Builder addExternal(String resource, URI uri) {
            externals.put(resource, uri);
            return this;
        }

        @Override
        public HttpResourceLocator.Builder addExternalFromEnv(String envName) {
            final String values = System.getenv(envName);
            if (values != null) {
                for(final var part : values.split("::")) {
                    final var pair = part.split("=");
                    if (pair.length != 2)
                        continue;
                    System.out.printf("Add external %.4096s=%.4096s from envvars%n", pair[0], pair[1]);
                    addExternal(pair[0], URI.create(pair[1]));
                }
            }
            return this;
        }

        @Override
        public HttpResourceLocator build() {
            return new HttpResourceLocatorImpl(this);
        }
    }
}
