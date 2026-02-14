package com.sta.core.engine;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Result of a code analysis run.
 */
public class AnalysisResult {

    private final String projectName;
    private final Path sourcePath;
    private final Instant analysisTime;
    private final Duration analysisDuration;
    private final List<Issue> issues;
    private final Map<String, Integer> fileCount;
    private final int totalLinesOfCode;
    private final Set<String> analyzersUsed;

    private AnalysisResult(Builder builder) {
        this.projectName = builder.projectName != null ? builder.projectName : "Unknown";
        this.sourcePath = builder.sourcePath;
        this.analysisTime = builder.analysisTime != null ? builder.analysisTime : Instant.now();
        this.analysisDuration = builder.analysisDuration;
        this.issues = builder.issues != null ? new ArrayList<>(builder.issues) : new ArrayList<>();
        this.fileCount = builder.fileCount != null ? new HashMap<>(builder.fileCount) : new HashMap<>();
        this.totalLinesOfCode = builder.totalLinesOfCode;
        this.analyzersUsed = builder.analyzersUsed != null ? new HashSet<>(builder.analyzersUsed) : new HashSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getProjectName() { return projectName; }
    public Path getSourcePath() { return sourcePath; }
    public Instant getAnalysisTime() { return analysisTime; }
    public Duration getAnalysisDuration() { return analysisDuration; }
    public List<Issue> getIssues() { return Collections.unmodifiableList(issues); }
    public Map<String, Integer> getFileCount() { return Collections.unmodifiableMap(fileCount); }
    public int getTotalLinesOfCode() { return totalLinesOfCode; }
    public Set<String> getAnalyzersUsed() { return Collections.unmodifiableSet(analyzersUsed); }

    /**
     * Returns total issue count.
     */
    public int getTotalIssueCount() {
        return issues.size();
    }

    /**
     * Returns issue count for a specific severity.
     */
    public long getIssueCount(Severity severity) {
        return issues.stream().filter(i -> i.getSeverity() == severity).count();
    }

    /**
     * Returns issue counts grouped by severity.
     */
    public Map<Severity, Long> getIssueCountBySeverity() {
        Map<Severity, Long> counts = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) {
            counts.put(s, 0L);
        }
        for (Issue issue : issues) {
            counts.merge(issue.getSeverity(), 1L, Long::sum);
        }
        return counts;
    }

    /**
     * Returns issues filtered by severity.
     */
    public List<Issue> getIssuesBySeverity(Severity severity) {
        return issues.stream()
                .filter(i -> i.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /**
     * Returns top violated rules with counts.
     */
    public List<Map.Entry<String, Long>> getTopViolatedRules(int limit) {
        Map<String, Long> ruleCounts = new HashMap<>();
        for (Issue issue : issues) {
            ruleCounts.merge(issue.getRule(), 1L, Long::sum);
        }

        return ruleCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("AnalysisResult{project='%s', issues=%d, lines=%d, duration=%s}",
                projectName, issues.size(), totalLinesOfCode,
                analysisDuration != null ? analysisDuration.getSeconds() + "s" : "N/A");
    }

    public static class Builder {
        private String projectName;
        private Path sourcePath;
        private Instant analysisTime;
        private Duration analysisDuration;
        private List<Issue> issues;
        private Map<String, Integer> fileCount;
        private int totalLinesOfCode;
        private Set<String> analyzersUsed;

        public Builder projectName(String projectName) { this.projectName = projectName; return this; }
        public Builder sourcePath(Path sourcePath) { this.sourcePath = sourcePath; return this; }
        public Builder analysisTime(Instant analysisTime) { this.analysisTime = analysisTime; return this; }
        public Builder analysisDuration(Duration analysisDuration) { this.analysisDuration = analysisDuration; return this; }
        public Builder issues(List<Issue> issues) { this.issues = issues; return this; }
        public Builder fileCount(Map<String, Integer> fileCount) { this.fileCount = fileCount; return this; }
        public Builder totalLinesOfCode(int totalLinesOfCode) { this.totalLinesOfCode = totalLinesOfCode; return this; }
        public Builder analyzersUsed(Set<String> analyzersUsed) { this.analyzersUsed = analyzersUsed; return this; }

        public AnalysisResult build() {
            return new AnalysisResult(this);
        }
    }
}
