package com.sta.core.source;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Source provider for Git repositories.
 */
public class GitSourceProvider implements SourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(GitSourceProvider.class);

    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?github\\.com/([^/]+)/([^/]+?)(?:\\.git)?/?$"
    );

    private SourceMetadata metadata;

    @Override
    public String getType() {
        return "git";
    }

    @Override
    public boolean canHandle(String sourceIdentifier) {
        return sourceIdentifier != null && (
                sourceIdentifier.startsWith("https://github.com/") ||
                sourceIdentifier.startsWith("http://github.com/") ||
                sourceIdentifier.startsWith("git@github.com:") ||
                sourceIdentifier.endsWith(".git")
        );
    }

    @Override
    public Path retrieve(String sourceIdentifier, Consumer<Double> progressCallback)
            throws SourceRetrievalException {

        logger.info("Cloning repository: {}", sourceIdentifier);

        try {
            // Parse repo info
            String repoUrl = normalizeUrl(sourceIdentifier);
            String repoName = extractRepoName(sourceIdentifier);

            this.metadata = new SourceMetadata(repoName, "", sourceIdentifier);

            // Create temp directory
            Path targetDir = Files.createTempDirectory("sta-" + repoName + "-");
            targetDir.toFile().deleteOnExit();

            logger.info("Cloning to: {}", targetDir);

            // Clone repository
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(targetDir.toFile())
                    .setProgressMonitor(new JGitProgressMonitor(progressCallback));

            Git git = cloneCommand.call();
            git.close();

            logger.info("Clone completed successfully");
            progressCallback.accept(1.0);

            return targetDir;

        } catch (Exception e) {
            logger.error("Failed to clone repository: {}", e.getMessage(), e);
            throw new SourceRetrievalException("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<SourceMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    private String normalizeUrl(String sourceIdentifier) {
        if (!sourceIdentifier.startsWith("http")) {
            // Convert git@github.com:user/repo to https://
            if (sourceIdentifier.startsWith("git@github.com:")) {
                String path = sourceIdentifier.substring("git@github.com:".length());
                return "https://github.com/" + path;
            }
        }
        if (!sourceIdentifier.endsWith(".git")) {
            return sourceIdentifier + ".git";
        }
        return sourceIdentifier;
    }

    private String extractRepoName(String sourceIdentifier) {
        Matcher matcher = GITHUB_PATTERN.matcher(sourceIdentifier);
        if (matcher.find()) {
            return matcher.group(2);
        }

        // Fallback: extract from URL
        String name = sourceIdentifier;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        if (name.endsWith(".git")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    /**
     * Progress monitor that maps JGit progress to our callback.
     */
    private static class JGitProgressMonitor implements ProgressMonitor {
        private final Consumer<Double> callback;
        private int totalWork;
        private int completed;

        JGitProgressMonitor(Consumer<Double> callback) {
            this.callback = callback;
        }

        @Override
        public void start(int totalTasks) {
            this.totalWork = 0;
            this.completed = 0;
        }

        @Override
        public void beginTask(String title, int totalWork) {
            this.totalWork = totalWork;
            this.completed = 0;
        }

        @Override
        public void update(int completed) {
            this.completed += completed;
            if (totalWork > 0) {
                callback.accept(Math.min(0.95, (double) this.completed / totalWork));
            }
        }

        @Override
        public void endTask() {
            callback.accept(0.95);
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
