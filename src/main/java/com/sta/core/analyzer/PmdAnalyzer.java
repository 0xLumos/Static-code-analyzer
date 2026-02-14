package com.sta.core.analyzer;

import com.sta.config.RuleConfiguration;
import com.sta.core.engine.Issue;
import com.sta.core.engine.Severity;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;

/**
 * PMD analyzer implementation for Java code.
 */
public class PmdAnalyzer implements Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(PmdAnalyzer.class);

    @Override
    public String getId() {
        return "pmd";
    }

    @Override
    public String getDisplayName() {
        return "PMD";
    }

    @Override
    public String getDescription() {
        return "Checks for code style, best practices, and potential bugs";
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName("net.sourceforge.pmd.PMD");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<Issue> analyze(Path sourcePath, RuleConfiguration config, Consumer<Double> progressCallback) {
        List<Issue> issues = new ArrayList<>();

        try {
            logger.info("Running PMD analysis on: {}", sourcePath);

            // Collect Java files
            List<DataSource> dataSources = collectJavaFiles(sourcePath);
            logger.info("Found {} Java files to analyze", dataSources.size());

            if (dataSources.isEmpty()) {
                logger.warn("No Java files found in: {}", sourcePath);
                progressCallback.accept(1.0);
                return issues;
            }

            // Configure PMD
            PMDConfiguration pmdConfig = new PMDConfiguration();
            pmdConfig.setSourceEncoding("UTF-8");
            pmdConfig.setThreads(Runtime.getRuntime().availableProcessors());
            pmdConfig.setMinimumPriority(RulePriority.LOW);

            // Add rule sets from configuration
            StringBuilder ruleSets = new StringBuilder();
            for (String ruleSet : config.getEnabledRuleSets()) {
                if (ruleSets.length() > 0) {
                    ruleSets.append(",");
                }
                ruleSets.append(ruleSet);
            }
            pmdConfig.setRuleSets(ruleSets.toString());

            // Create our custom renderer to collect violations
            IssueCollectingRenderer renderer = new IssueCollectingRenderer(issues, sourcePath, progressCallback, dataSources.size());

            // Run PMD
            try {
                RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.defaultFactory();
                RuleSets ruleSetsCombined = ruleSetFactory.createRuleSets(pmdConfig.getRuleSets());

                RuleContext ctx = new RuleContext();

                int processed = 0;
                for (DataSource dataSource : dataSources) {
                    String fileName = dataSource.getNiceFileName(false, null);
                    try {
                        ctx.setSourceCodeFile(new File(fileName));

                        // Parse and analyze
                        for (RuleSet ruleSet : ruleSetsCombined.getAllRuleSets()) {
                            PMD.processFiles(pmdConfig, ruleSetFactory, Arrays.asList(dataSource), ctx,
                                    Collections.singletonList(renderer));
                        }
                    } catch (Exception e) {
                        logger.debug("Error analyzing {}: {}", fileName, e.getMessage());
                    }

                    processed++;
                    progressCallback.accept((double) processed / dataSources.size() * 0.95);
                }

                renderer.end();

            } catch (Exception e) {
                logger.error("PMD rule set loading failed: {}", e.getMessage(), e);
            }

            logger.info("PMD analysis completed with {} issues", issues.size());

        } catch (Exception e) {
            logger.error("PMD analysis failed: {}", e.getMessage(), e);
        }

        progressCallback.accept(1.0);
        return issues;
    }

    private List<DataSource> collectJavaFiles(Path sourcePath) throws IOException {
        List<DataSource> dataSources = new ArrayList<>();

        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".java")) {
                    dataSources.add(new FileDataSource(file.toFile()));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String name = dir.getFileName().toString();
                // Skip common non-source directories
                if (name.equals(".git") || name.equals("target") ||
                    name.equals("build") || name.equals("node_modules") ||
                    name.equals(".idea") || name.equals(".gradle")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return dataSources;
    }

    /**
     * Custom renderer that collects violations as Issue objects.
     */
    private static class IssueCollectingRenderer extends AbstractIncrementingRenderer {
        private final List<Issue> issues;
        private final Path basePath;
        private final Consumer<Double> progressCallback;
        private final int totalFiles;
        private int processedFiles = 0;

        IssueCollectingRenderer(List<Issue> issues, Path basePath, Consumer<Double> progressCallback, int totalFiles) {
            super("issue-collector", "Collects issues");
            this.issues = issues;
            this.basePath = basePath;
            this.progressCallback = progressCallback;
            this.totalFiles = totalFiles;
        }

        @Override
        public String defaultFileExtension() {
            return null;
        }

        @Override
        public void renderFileViolations(Iterator<RuleViolation> violations) {
            while (violations.hasNext()) {
                RuleViolation v = violations.next();

                String filePath = v.getFilename();
                String relativePath = filePath;
                try {
                    relativePath = basePath.relativize(Paths.get(filePath)).toString();
                } catch (Exception e) {
                    // Use absolute path if relativization fails
                }

                Issue issue = Issue.builder()
                        .filePath(filePath)
                        .startLine(v.getBeginLine())
                        .endLine(v.getEndLine())
                        .startColumn(v.getBeginColumn())
                        .endColumn(v.getEndColumn())
                        .rule(v.getRule().getName())
                        .ruleSet(v.getRule().getRuleSetName())
                        .message(v.getDescription())
                        .severity(mapPriority(v.getRule().getPriority()))
                        .analyzer("PMD")
                        .build();

                issues.add(issue);
            }

            processedFiles++;
            progressCallback.accept((double) processedFiles / totalFiles * 0.95);
        }

        @Override
        public void start() {}

        @Override
        public void startFileAnalysis(DataSource dataSource) {}

        @Override
        public void end() {}

        @Override
        public void setWriter(Writer writer) {}

        @Override
        public Writer getWriter() {
            return null;
        }

        private Severity mapPriority(RulePriority priority) {
            switch (priority) {
                case HIGH:
                    return Severity.CRITICAL;
                case MEDIUM_HIGH:
                    return Severity.HIGH;
                case MEDIUM:
                    return Severity.MEDIUM;
                case MEDIUM_LOW:
                    return Severity.LOW;
                case LOW:
                default:
                    return Severity.INFO;
            }
        }
    }
}
