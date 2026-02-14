package com.sta.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration for analysis rules.
 * Supports enabling/disabling rules and customizing severity levels.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleConfiguration.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
    
    private Set<String> enabledRuleSets = new HashSet<>();
    private Set<String> disabledRules = new HashSet<>();
    private Map<String, String> severityOverrides = new HashMap<>();
    private Map<String, Map<String, Object>> ruleProperties = new HashMap<>();
    
    public RuleConfiguration() {
        // Initialize with default rule sets
        enabledRuleSets.add("category/java/bestpractices.xml");
        enabledRuleSets.add("category/java/codestyle.xml");
        enabledRuleSets.add("category/java/design.xml");
        enabledRuleSets.add("category/java/errorprone.xml");
        enabledRuleSets.add("category/java/performance.xml");
        enabledRuleSets.add("category/java/security.xml");
    }
    
    // Getters and Setters
    public Set<String> getEnabledRuleSets() { return enabledRuleSets; }
    public void setEnabledRuleSets(Set<String> enabledRuleSets) { 
        this.enabledRuleSets = enabledRuleSets; 
    }
    
    public Set<String> getDisabledRules() { return disabledRules; }
    public void setDisabledRules(Set<String> disabledRules) { 
        this.disabledRules = disabledRules; 
    }
    
    public Map<String, String> getSeverityOverrides() { return severityOverrides; }
    public void setSeverityOverrides(Map<String, String> severityOverrides) { 
        this.severityOverrides = severityOverrides; 
    }
    
    public Map<String, Map<String, Object>> getRuleProperties() { return ruleProperties; }
    public void setRuleProperties(Map<String, Map<String, Object>> ruleProperties) { 
        this.ruleProperties = ruleProperties; 
    }
    
    // Fluent API
    public RuleConfiguration enableRuleSet(String ruleSet) {
        enabledRuleSets.add(ruleSet);
        return this;
    }
    
    public RuleConfiguration disableRule(String ruleName) {
        disabledRules.add(ruleName);
        return this;
    }
    
    public RuleConfiguration enableRule(String ruleName) {
        disabledRules.remove(ruleName);
        return this;
    }
    
    public RuleConfiguration overrideSeverity(String ruleName, String severity) {
        severityOverrides.put(ruleName, severity);
        return this;
    }
    
    public RuleConfiguration setRuleProperty(String ruleName, String property, Object value) {
        ruleProperties.computeIfAbsent(ruleName, k -> new HashMap<>()).put(property, value);
        return this;
    }
    
    public boolean isRuleEnabled(String ruleName) {
        return !disabledRules.contains(ruleName);
    }
    
    public Optional<String> getSeverityOverride(String ruleName) {
        return Optional.ofNullable(severityOverrides.get(ruleName));
    }
    
    /**
     * Loads configuration from a JSON file.
     */
    public static RuleConfiguration fromFile(Path configPath) throws IOException {
        logger.info("Loading rule configuration from: {}", configPath);
        String content = new String(Files.readAllBytes(configPath), java.nio.charset.StandardCharsets.UTF_8);
        return objectMapper.readValue(content, RuleConfiguration.class);
    }
    
    /**
     * Loads configuration from classpath resource.
     */
    public static RuleConfiguration fromResource(String resourcePath) throws IOException {
        logger.info("Loading rule configuration from resource: {}", resourcePath);
        try (InputStream is = RuleConfiguration.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return objectMapper.readValue(is, RuleConfiguration.class);
        }
    }
    
    /**
     * Saves configuration to a JSON file.
     */
    public void save(Path configPath) throws IOException {
        logger.info("Saving rule configuration to: {}", configPath);
        objectMapper.writeValue(configPath.toFile(), this);
    }
    
    /**
     * Creates a default configuration.
     */
    public static RuleConfiguration defaults() {
        return new RuleConfiguration();
    }
    
    /**
     * Creates a minimal configuration optimized for quick analysis.
     */
    public static RuleConfiguration minimal() {
        RuleConfiguration config = new RuleConfiguration();
        config.enabledRuleSets.clear();
        config.enabledRuleSets.add("category/java/errorprone.xml");
        config.enabledRuleSets.add("category/java/security.xml");
        return config;
    }
    
    /**
     * Creates a strict configuration for high code quality standards.
     */
    public static RuleConfiguration strict() {
        RuleConfiguration config = new RuleConfiguration();
        config.enabledRuleSets.add("category/java/documentation.xml");
        config.enabledRuleSets.add("category/java/multithreading.xml");
        return config;
    }
}
