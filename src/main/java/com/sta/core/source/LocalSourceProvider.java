package com.sta.core.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Source provider for local file system paths.
 */
public class LocalSourceProvider implements SourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalSourceProvider.class);

    private SourceMetadata metadata;

    @Override
    public String getType() {
        return "local";
    }

    @Override
    public boolean canHandle(String sourceIdentifier) {
        if (sourceIdentifier == null) return false;

        // Check for absolute paths
        Path path = Paths.get(sourceIdentifier);
        return Files.exists(path) && Files.isDirectory(path);
    }

    @Override
    public Path retrieve(String sourceIdentifier, Consumer<Double> progressCallback)
            throws SourceRetrievalException {

        logger.info("Using local path: {}", sourceIdentifier);

        Path path = Paths.get(sourceIdentifier);

        if (!Files.exists(path)) {
            throw new SourceRetrievalException("Path does not exist: " + sourceIdentifier);
        }

        if (!Files.isDirectory(path)) {
            throw new SourceRetrievalException("Path is not a directory: " + sourceIdentifier);
        }

        String name = path.getFileName() != null
                ? path.getFileName().toString()
                : "LocalProject";

        this.metadata = new SourceMetadata(name, "Local project", path.toAbsolutePath().toString());

        progressCallback.accept(1.0);
        return path;
    }

    @Override
    public Optional<SourceMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }
}
