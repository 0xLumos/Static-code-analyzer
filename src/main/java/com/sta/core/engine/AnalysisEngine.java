package com.sta.core.engine;

import com.sta.config.RuleConfiguration;
import com.sta.core.analyzer.Analyzer;
import com.sta.core.analyzer.PmdAnalyzer;
import com.sta.core.source.GitSourceProvider;
import com.sta.core.source.LocalSourceProvider;
import com.sta.core.source.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Main orchestrator for code analysis.
 * Manages source retrieval and coordinates multiple analyzers.
 */
public class AnalysisEngine {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisEngine.class);

    private final List<Analyzer> analyzers = new ArrayList<>();
    private final List<SourceProvider> sourceProviders = new ArrayList<>();
    private final ExecutorService executor;
    private RuleConfiguration ruleConfiguration;

    private Consumer<String> statusCallback;
    private Consumer<Double> progressCallback;

    public AnalysisEngine() {
        this.executor = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() - 1)
        );
        this.ruleConfiguration = RuleConfiguration.defaults();
        this.statusCallback = status -> {};
        this.progressCallback = progress -> {};

        // Register default analyzers
        registerAnalyzer(new PmdAnalyzer());

        // Register default source providers
        registerSourceProvider(new GitSourceProvider());
        registerSourceProvider(new LocalSourceProvider());
    }

    public AnalysisEngine registerAnalyzer(Analyzer analyzer) {
        analyzers.add(analyzer);
        logger.info("Registered analyzer: {} (available: {})",
                analyzer.getDisplayName(), analyzer.isAvailable());
        return this;
    }

    public AnalysisEngine registerSourceProvider(SourceProvider provider) {
        sourceProviders.add(provider);
        logger.info("Registered source provider: {}", provider.getType());
        return this;
    }

    public AnalysisEngine setRuleConfiguration(RuleConfiguration config) {
        this.ruleConfiguration = config;
        return this;
    }

    public AnalysisEngine onStatus(Consumer<String> callback) {
        this.statusCallback = callback;
        return this;
    }

    public AnalysisEngine onProgress(Consumer<Double> callback) {
        this.progressCallback = callback;
        return this;
    }

    /**
     * Analyzes source code from the given identifier.
     */
    public CompletableFuture<AnalysisResult> analyze(String sourceIdentifier) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();

            try {
                // Find appropriate source provider
                statusCallback.accept("Detecting source type...");
                progressCallback.accept(0.0);

                SourceProvider provider = findProvider(sourceIdentifier);
                if (provider == null) {
                    throw new IllegalArgumentException(
                            "No provider found for source: " + sourceIdentifier);
                }

                // Retrieve source code
                statusCallback.accept("Retrieving source code...");
                progressCallback.accept(0.05);

                Path sourcePath = provider.retrieve(sourceIdentifier, progress ->
                        progressCallback.accept(0.05 + progress * 0.15)
                );

                // Count files and lines
                statusCallback.accept("Scanning source files...");
                progressCallback.accept(0.2);

                Map<String, Integer> fileCount = countFiles(sourcePath);
                int totalLines = countLines(sourcePath);

                // Determine project name
                String projectName = provider.getMetadata()
                        .map(m -> m.getName())
                        .orElse(sourcePath.getFileName().toString());

                // Run analyzers
                statusCallback.accept("Running code analysis...");
                progressCallback.accept(0.25);

                List<Issue> allIssues = Collections.synchronizedList(new ArrayList<>());
                Set<String> analyzersUsed = Collections.synchronizedSet(new HashSet<>());

                List<Analyzer> availableAnalyzers = new ArrayList<>();
                for (Analyzer analyzer : analyzers) {
                    if (analyzer.isAvailable()) {
                        availableAnalyzers.add(analyzer);
                    }
                }

                if (availableAnalyzers.isEmpty()) {
                    logger.warn("No analyzers available!");
                }

                double progressPerAnalyzer = 0.7 / Math.max(1, availableAnalyzers.size());
                int analyzerIndex = 0;

                for (Analyzer analyzer : availableAnalyzers) {
                    final int currentIndex = analyzerIndex++;

                    statusCallback.accept("Running " + analyzer.getDisplayName() + "...");

                    try {
                        double baseProgress = 0.25 + currentIndex * progressPerAnalyzer;

                        List<Issue> issues = analyzer.analyze(sourcePath, ruleConfiguration,
                                progress -> progressCallback.accept(
                                        baseProgress + progress * progressPerAnalyzer
                                )
                        );

                        allIssues.addAll(issues);
                        analyzersUsed.add(analyzer.getDisplayName());

                        logger.info("{} found {} issues", analyzer.getDisplayName(), issues.size());

                    } catch (Exception e) {
                        logger.error("{} failed: {}", analyzer.getDisplayName(), e.getMessage(), e);
                    }
                }

                // Build result
                statusCallback.accept("Generating report...");
                progressCallback.accept(0.95);

                Duration duration = Duration.between(startTime, Instant.now());

                AnalysisResult result = AnalysisResult.builder()
                        .projectName(projectName)
                        .sourcePath(sourcePath)
                        .analysisTime(startTime)
                        .analysisDuration(duration)
                        .issues(allIssues)
                        .fileCount(fileCount)
                        .totalLinesOfCode(totalLines)
                        .analyzersUsed(analyzersUsed)
                        .build();

                statusCallback.accept("Analysis complete!");
                progressCallback.accept(1.0);

                logger.info("Analysis completed: {}", result);

                return result;

            } catch (SourceProvider.SourceRetrievalException e) {
                logger.error("Source retrieval failed: {}", e.getMessage(), e);
                throw new CompletionException(e);
            } catch (Exception e) {
                logger.error("Analysis failed: {}", e.getMessage(), e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Shuts down the engine and releases resources.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private SourceProvider findProvider(String sourceIdentifier) {
        for (SourceProvider provider : sourceProviders) {
            if (provider.canHandle(sourceIdentifier)) {
                return provider;
            }
        }
        return null;
    }

    private Map<String, Integer> countFiles(Path sourcePath) {
        Map<String, Integer> counts = new HashMap<>();

        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String name = file.getFileName().toString();
                    int dot = name.lastIndexOf('.');
                    String ext = dot > 0 ? name.substring(dot + 1).toLowerCase() : "other";
                    counts.merge(ext, 1, Integer::sum);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.warn("Failed to count files: {}", e.getMessage());
        }

        return counts;
    }

    private int countLines(Path sourcePath) {
        final int[] totalLines = {0};

        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String name = file.toString().toLowerCase();
                    if (name.endsWith(".java") || name.endsWith(".kt") ||
                        name.endsWith(".scala") || name.endsWith(".groovy")) {
                        try {
                            totalLines[0] += Files.readAllLines(file).size();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.warn("Failed to count lines: {}", e.getMessage());
        }

        return totalLines[0];
    }

    public List<Analyzer> getAnalyzers() {
        return Collections.unmodifiableList(analyzers);
    }

    public RuleConfiguration getRuleConfiguration() {
        return ruleConfiguration;
    }
}
