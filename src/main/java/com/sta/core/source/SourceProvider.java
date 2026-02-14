package com.sta.core.source;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Interface for source code providers.
 * Implementations handle different source types (Git, local, etc.).
 */
public interface SourceProvider {

    /**
     * Returns the type of this provider.
     */
    String getType();

    /**
     * Checks if this provider can handle the given identifier.
     */
    boolean canHandle(String sourceIdentifier);

    /**
     * Retrieves source code to a local path.
     */
    Path retrieve(String sourceIdentifier, Consumer<Double> progressCallback)
            throws SourceRetrievalException;

    /**
     * Returns metadata about the source if available.
     */
    Optional<SourceMetadata> getMetadata();

    /**
     * Exception for source retrieval failures.
     */
    class SourceRetrievalException extends Exception {
        public SourceRetrievalException(String message) {
            super(message);
        }

        public SourceRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Metadata about a source.
     */
    class SourceMetadata {
        private final String name;
        private final String description;
        private final String url;

        public SourceMetadata(String name, String description, String url) {
            this.name = name;
            this.description = description;
            this.url = url;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
    }
}
