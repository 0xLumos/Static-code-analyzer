package com.sta.cli;

import com.sta.config.RuleConfiguration;
import com.sta.core.engine.AnalysisEngine;
import com.sta.core.engine.AnalysisResult;
import com.sta.core.engine.Severity;
import com.sta.report.HtmlReportGenerator;
import com.sta.util.QualityScoreCalculator;
import com.sta.util.QualityScoreCalculator.QualityBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * CLI runner for headless analysis and CI/CD integration.
 */
@Command(
    name = "sta",
    description = "Static Code Analyzer - CLI Mode",
    mixinStandardHelpOptions = true,
    version = "Static Code Analyzer 2.0.0"
)
public class CliRunner implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);

    @Option(names = {"-u", "--url"}, description = "GitHub repository URL")
    private String url;

    @Option(names = {"-p", "--path"}, description = "Local project path")
    private String path;

    @Option(names = {"-f", "--format"}, description = "Report format: html, json, csv", defaultValue = "html")
    private String format;

    @Option(names = {"-o", "--output"}, description = "Output file path")
    private String output;

    @Option(names = {"--fail-on"}, description = "Fail if issues of this severity found: CRITICAL, HIGH, MEDIUM, LOW")
    private String failOn;

    @Option(names = {"--min-score"}, description = "Minimum quality score required (0-100)")
    private Integer minScore;

    @Option(names = {"-q", "--quiet"}, description = "Suppress output except errors")
    private boolean quiet;

    @Override
    public Integer call() {
        // Determine source
        String source = url != null ? url : path;
        if (source == null) {
            System.err.println("Error: Please specify --url or --path");
            return 1;
        }

        if (!quiet) {
            System.out.println("🔬 Static Code Analyzer v2.0.0");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📂 Source: " + source);
        }

        try {
            // Run analysis
            AnalysisEngine engine = new AnalysisEngine();

            if (!quiet) {
                engine.onStatus(status -> System.out.println("⏳ " + status));
            }

            AnalysisResult result = engine.analyze(source).join();
            QualityBreakdown breakdown = QualityScoreCalculator.calculateBreakdown(result);

            // Print summary
            if (!quiet) {
                printSummary(result, breakdown);
            }

            // Generate report
            if (output != null) {
                Path outputPath = Paths.get(output);
                HtmlReportGenerator generator = new HtmlReportGenerator();
                generator.generate(result, outputPath);
                if (!quiet) {
                    System.out.println("\n📄 Report saved to: " + outputPath.toAbsolutePath());
                }
            }

            // Check quality gates
            if (failOn != null) {
                Severity failSeverity = Severity.fromString(failOn);
                long count = countIssuesAtOrAbove(result, failSeverity);
                if (count > 0) {
                    System.err.println("\n❌ FAILED: Found " + count + " issues at or above " + failSeverity.getDisplayName());
                    return 2;
                }
            }

            if (minScore != null && breakdown.score() < minScore) {
                System.err.println("\n❌ FAILED: Score " + String.format("%.1f", breakdown.score()) +
                        " is below minimum " + minScore);
                return 3;
            }

            if (!quiet) {
                System.out.println("\n✅ Analysis completed successfully!");
            }

            return 0;

        } catch (Exception e) {
            System.err.println("❌ Analysis failed: " + e.getMessage());
            logger.error("CLI analysis failed", e);
            return 1;
        }
    }

    private void printSummary(AnalysisResult result, QualityBreakdown breakdown) {
        System.out.println();
        System.out.println("📊 ANALYSIS SUMMARY");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("   Project:     " + result.getProjectName());
        System.out.println("   Lines:       " + String.format("%,d", breakdown.linesOfCode()));
        System.out.println("   Files:       " + result.getFileCount().values().stream()
                .mapToInt(Integer::intValue).sum());
        System.out.println();
        System.out.println("📈 QUALITY SCORE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("   Grade:       " + breakdown.grade() + " (" + breakdown.gradeDescription() + ")");
        System.out.println("   Score:       " + String.format("%.1f / 100", breakdown.score()));
        System.out.println("   Issues/KLOC: " + String.format("%.2f", breakdown.issuesPerKLoc()));
        System.out.println();
        System.out.println("🔍 ISSUES BY SEVERITY");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("   🔴 Critical:  " + breakdown.criticalCount());
        System.out.println("   🟠 High:      " + breakdown.highCount());
        System.out.println("   🟡 Medium:    " + breakdown.mediumCount());
        System.out.println("   🔵 Low:       " + breakdown.lowCount());
        System.out.println("   ⚪ Info:      " + breakdown.infoCount());
        System.out.println("   ─────────────────");
        System.out.println("   📋 Total:     " + breakdown.totalIssues());
    }

    private long countIssuesAtOrAbove(AnalysisResult result, Severity threshold) {
        return result.getIssues().stream()
                .filter(i -> i.getSeverity().getWeight() >= threshold.getWeight())
                .count();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CliRunner()).execute(args);
        System.exit(exitCode);
    }
}
