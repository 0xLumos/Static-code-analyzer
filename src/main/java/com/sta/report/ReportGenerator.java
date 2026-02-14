package com.sta.report;

import com.sta.core.engine.AnalysisResult;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for report generators.
 */
public interface ReportGenerator {

    /**
     * Returns the format name (e.g., "html", "json", "csv").
     */
    String getFormat();

    /**
     * Generates a report and writes it to the specified path.
     */
    void generate(AnalysisResult result, Path outputPath) throws IOException;

    /**
     * Generates a report and returns it as a string.
     */
    String generateToString(AnalysisResult result);
}
