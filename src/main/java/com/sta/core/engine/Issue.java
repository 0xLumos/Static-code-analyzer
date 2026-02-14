package com.sta.core.engine;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single issue found during code analysis.
 */
public class Issue {

    private final String id;
    private final Severity severity;
    private final String rule;
    private final String ruleSet;
    private final String message;
    private final String description;
    private final String filePath;
    private final int startLine;
    private final int endLine;
    private final int startColumn;
    private final int endColumn;
    private final String codeSnippet;
    private final String suggestion;
    private final String analyzer;
    private final String documentationUrl;
    private final Instant detectedAt;

    private Issue(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.severity = builder.severity != null ? builder.severity : Severity.INFO;
        this.rule = Objects.requireNonNull(builder.rule, "Rule cannot be null");
        this.ruleSet = builder.ruleSet;
        this.message = Objects.requireNonNull(builder.message, "Message cannot be null");
        this.description = builder.description;
        this.filePath = builder.filePath;
        this.startLine = builder.startLine;
        this.endLine = builder.endLine > 0 ? builder.endLine : builder.startLine;
        this.startColumn = builder.startColumn;
        this.endColumn = builder.endColumn;
        this.codeSnippet = builder.codeSnippet;
        this.suggestion = builder.suggestion;
        this.analyzer = builder.analyzer != null ? builder.analyzer : "Unknown";
        this.documentationUrl = builder.documentationUrl;
        this.detectedAt = builder.detectedAt != null ? builder.detectedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getId() { return id; }
    public Severity getSeverity() { return severity; }
    public String getRule() { return rule; }
    public String getRuleSet() { return ruleSet; }
    public String getMessage() { return message; }
    public String getDescription() { return description; }
    public String getFilePath() { return filePath; }
    public int getStartLine() { return startLine; }
    public int getEndLine() { return endLine; }
    public int getStartColumn() { return startColumn; }
    public int getEndColumn() { return endColumn; }
    public String getCodeSnippet() { return codeSnippet; }
    public String getSuggestion() { return suggestion; }
    public String getAnalyzer() { return analyzer; }
    public String getDocumentationUrl() { return documentationUrl; }
    public Instant getDetectedAt() { return detectedAt; }

    /**
     * Returns just the file name without the path.
     */
    public String getFileName() {
        if (filePath == null || filePath.isEmpty()) {
            return "Unknown";
        }
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * Returns a formatted location string.
     */
    public String getLocation() {
        return getFileName() + ":" + startLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return startLine == issue.startLine &&
               startColumn == issue.startColumn &&
               Objects.equals(rule, issue.rule) &&
               Objects.equals(filePath, issue.filePath) &&
               Objects.equals(analyzer, issue.analyzer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rule, filePath, startLine, startColumn, analyzer);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s at %s:%d",
                severity.getDisplayName(), rule, message, getFileName(), startLine);
    }

    public static class Builder {
        private String id;
        private Severity severity;
        private String rule;
        private String ruleSet;
        private String message;
        private String description;
        private String filePath;
        private int startLine;
        private int endLine;
        private int startColumn;
        private int endColumn;
        private String codeSnippet;
        private String suggestion;
        private String analyzer;
        private String documentationUrl;
        private Instant detectedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder severity(Severity severity) { this.severity = severity; return this; }
        public Builder rule(String rule) { this.rule = rule; return this; }
        public Builder ruleSet(String ruleSet) { this.ruleSet = ruleSet; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder startLine(int startLine) { this.startLine = startLine; return this; }
        public Builder endLine(int endLine) { this.endLine = endLine; return this; }
        public Builder startColumn(int startColumn) { this.startColumn = startColumn; return this; }
        public Builder endColumn(int endColumn) { this.endColumn = endColumn; return this; }
        public Builder codeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; return this; }
        public Builder suggestion(String suggestion) { this.suggestion = suggestion; return this; }
        public Builder analyzer(String analyzer) { this.analyzer = analyzer; return this; }
        public Builder documentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl; return this; }
        public Builder detectedAt(Instant detectedAt) { this.detectedAt = detectedAt; return this; }

        public Issue build() {
            return new Issue(this);
        }
    }
}
