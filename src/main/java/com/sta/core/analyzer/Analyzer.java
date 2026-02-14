package com.sta.core.analyzer;

import com.sta.config.RuleConfiguration;
import com.sta.core.engine.Issue;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for code analyzers.
 * Implementations wrap specific analysis tools (PMD, SpotBugs, Checkstyle).
 */
public interface Analyzer {

    /**
     * Returns the unique identifier of this analyzer.
     */
    String getId();

    /**
     * Returns a human-friendly display name.
     */
    String getDisplayName();

    /**
     * Checks if this analyzer is available and configured.
     */
    boolean isAvailable();

    /**
     * Analyzes the source code at the given path.
     */
    List<Issue> analyze(Path sourcePath, RuleConfiguration config, Consumer<Double> progressCallback);

    /**
     * Returns a description of what this analyzer checks.
     */
    default String getDescription() {
        return "Code analyzer";
    }
}
