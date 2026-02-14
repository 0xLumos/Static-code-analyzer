package com.sta.core.engine;

/**
 * Severity levels for code analysis issues.
 * Used for classification, filtering, and quality scoring.
 */
public enum Severity {

    CRITICAL(10.0, "Critical", "#DC2626", "🔴"),
    HIGH(5.0, "High", "#EA580C", "🟠"),
    MEDIUM(2.0, "Medium", "#CA8A04", "🟡"),
    LOW(0.5, "Low", "#2563EB", "🔵"),
    INFO(0.1, "Info", "#6B7280", "⚪");

    private final double weight;
    private final String displayName;
    private final String color;
    private final String icon;

    Severity(double weight, String displayName, String color, String icon) {
        this.weight = weight;
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
    }

    public double getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Parses a severity from common string representations.
     */
    public static Severity fromString(String value) {
        if (value == null) return INFO;

        String upper = value.trim().toUpperCase();

        switch (upper) {
            // Critical variations
            case "CRITICAL":
            case "BLOCKER":
            case "FATAL":
                return CRITICAL;

            // High variations
            case "HIGH":
            case "ERROR":
            case "MAJOR":
            case "SEVERE":
            case "1":
                return HIGH;

            // Medium variations
            case "MEDIUM":
            case "WARNING":
            case "WARN":
            case "MINOR":
            case "2":
            case "3":
                return MEDIUM;

            // Low variations
            case "LOW":
            case "TRIVIAL":
            case "COSMETIC":
            case "4":
                return LOW;

            // Info variations
            case "INFO":
            case "INFORMATION":
            case "SUGGESTION":
            case "HINT":
            case "5":
                return INFO;

            default:
                return INFO;
        }
    }

    /**
     * Maps PMD priority to severity.
     */
    public static Severity fromPmdPriority(int priority) {
        switch (priority) {
            case 1: return CRITICAL;
            case 2: return HIGH;
            case 3: return MEDIUM;
            case 4: return LOW;
            default: return INFO;
        }
    }
}
