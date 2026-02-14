package com.sta.util;

import com.sta.core.engine.AnalysisResult;
import com.sta.core.engine.Issue;
import com.sta.core.engine.Severity;

import java.util.*;

/**
 * Utility class for calculating code quality scores and grades.
 */
public class QualityScoreCalculator {

    private static final Map<String, GradeInfo> GRADE_THRESHOLDS;

    static {
        GRADE_THRESHOLDS = new LinkedHashMap<>();
        GRADE_THRESHOLDS.put("A+", new GradeInfo(95, 100, "#22C55E", "Excellent"));
        GRADE_THRESHOLDS.put("A",  new GradeInfo(90, 94,  "#22C55E", "Very Good"));
        GRADE_THRESHOLDS.put("A-", new GradeInfo(85, 89,  "#84CC16", "Good"));
        GRADE_THRESHOLDS.put("B+", new GradeInfo(80, 84,  "#84CC16", "Above Average"));
        GRADE_THRESHOLDS.put("B",  new GradeInfo(75, 79,  "#EAB308", "Average"));
        GRADE_THRESHOLDS.put("B-", new GradeInfo(70, 74,  "#EAB308", "Below Average"));
        GRADE_THRESHOLDS.put("C+", new GradeInfo(65, 69,  "#F97316", "Fair"));
        GRADE_THRESHOLDS.put("C",  new GradeInfo(60, 64,  "#F97316", "Needs Improvement"));
        GRADE_THRESHOLDS.put("C-", new GradeInfo(55, 59,  "#EF4444", "Poor"));
        GRADE_THRESHOLDS.put("D",  new GradeInfo(50, 54,  "#EF4444", "Very Poor"));
        GRADE_THRESHOLDS.put("F",  new GradeInfo(0,  49,  "#DC2626", "Failing"));
    }

    /**
     * Calculates a detailed quality score breakdown.
     */
    public static QualityBreakdown calculateBreakdown(AnalysisResult result) {
        Map<Severity, Long> counts = result.getIssueCountBySeverity();

        long critical = counts.getOrDefault(Severity.CRITICAL, 0L);
        long high = counts.getOrDefault(Severity.HIGH, 0L);
        long medium = counts.getOrDefault(Severity.MEDIUM, 0L);
        long low = counts.getOrDefault(Severity.LOW, 0L);
        long info = counts.getOrDefault(Severity.INFO, 0L);

        // Calculate penalty
        double penalty =
            critical * Severity.CRITICAL.getWeight() +
            high * Severity.HIGH.getWeight() +
            medium * Severity.MEDIUM.getWeight() +
            low * Severity.LOW.getWeight() +
            info * Severity.INFO.getWeight();

        // Normalize by lines of code
        int lines = result.getTotalLinesOfCode();
        double normalizationFactor = lines > 0 ? Math.log10(lines + 1) : 1.0;

        double normalizedPenalty = penalty / normalizationFactor;
        double score = Math.max(0.0, Math.min(100.0, 100.0 - normalizedPenalty));

        String grade = getGrade(score);
        GradeInfo gradeInfo = GRADE_THRESHOLDS.get(grade);

        return new QualityBreakdown(
            score, grade, gradeInfo.color, gradeInfo.description,
            penalty, normalizedPenalty,
            critical, high, medium, low, info, lines
        );
    }

    /**
     * Returns a letter grade based on the score.
     */
    public static String getGrade(double score) {
        for (Map.Entry<String, GradeInfo> entry : GRADE_THRESHOLDS.entrySet()) {
            GradeInfo info = entry.getValue();
            if (score >= info.minScore && score <= info.maxScore) {
                return entry.getKey();
            }
        }
        return "F";
    }

    /**
     * Returns the color associated with a grade.
     */
    public static String getGradeColor(String grade) {
        GradeInfo info = GRADE_THRESHOLDS.get(grade);
        return info != null ? info.color : "#6B7280";
    }

    /**
     * Grade threshold information.
     */
    public static class GradeInfo {
        public final int minScore;
        public final int maxScore;
        public final String color;
        public final String description;

        public GradeInfo(int minScore, int maxScore, String color, String description) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.color = color;
            this.description = description;
        }
    }

    /**
     * Quality breakdown with all metrics.
     */
    public static class QualityBreakdown {
        private final double score;
        private final String grade;
        private final String gradeColor;
        private final String gradeDescription;
        private final double rawPenalty;
        private final double normalizedPenalty;
        private final long criticalCount;
        private final long highCount;
        private final long mediumCount;
        private final long lowCount;
        private final long infoCount;
        private final int linesOfCode;

        public QualityBreakdown(double score, String grade, String gradeColor, String gradeDescription,
                                double rawPenalty, double normalizedPenalty,
                                long criticalCount, long highCount, long mediumCount,
                                long lowCount, long infoCount, int linesOfCode) {
            this.score = score;
            this.grade = grade;
            this.gradeColor = gradeColor;
            this.gradeDescription = gradeDescription;
            this.rawPenalty = rawPenalty;
            this.normalizedPenalty = normalizedPenalty;
            this.criticalCount = criticalCount;
            this.highCount = highCount;
            this.mediumCount = mediumCount;
            this.lowCount = lowCount;
            this.infoCount = infoCount;
            this.linesOfCode = linesOfCode;
        }

        public double score() { return score; }
        public String grade() { return grade; }
        public String gradeColor() { return gradeColor; }
        public String gradeDescription() { return gradeDescription; }
        public double rawPenalty() { return rawPenalty; }
        public double normalizedPenalty() { return normalizedPenalty; }
        public long criticalCount() { return criticalCount; }
        public long highCount() { return highCount; }
        public long mediumCount() { return mediumCount; }
        public long lowCount() { return lowCount; }
        public long infoCount() { return infoCount; }
        public int linesOfCode() { return linesOfCode; }

        public long totalIssues() {
            return criticalCount + highCount + mediumCount + lowCount + infoCount;
        }

        public double issuesPerKLoc() {
            if (linesOfCode == 0) return 0;
            return (totalIssues() * 1000.0) / linesOfCode;
        }
    }
}
