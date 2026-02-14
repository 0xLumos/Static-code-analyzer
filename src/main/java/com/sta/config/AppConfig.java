package com.sta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application configuration with persistence support.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static AppConfig instance;
    private static final String CONFIG_DIR = ".sta";
    private static final String CONFIG_FILE = "config.json";

    // UI settings
    private int windowWidth = 1400;
    private int windowHeight = 900;
    private String theme = "dark";
    private String lastOpenedPath;

    // Analysis settings
    private int maxConcurrentAnalyzers = 2;
    private boolean enablePmd = true;
    private boolean enableSpotBugs = true;
    private boolean enableCheckstyle = true;

    private AppConfig() {
        load();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void load() {
        try {
            Path configPath = getConfigPath();
            if (Files.exists(configPath)) {
                String content = new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8);
                AppConfig loaded = objectMapper.readValue(content, AppConfig.class);

                this.windowWidth = loaded.windowWidth;
                this.windowHeight = loaded.windowHeight;
                this.theme = loaded.theme;
                this.lastOpenedPath = loaded.lastOpenedPath;
                this.maxConcurrentAnalyzers = loaded.maxConcurrentAnalyzers;
                this.enablePmd = loaded.enablePmd;
                this.enableSpotBugs = loaded.enableSpotBugs;
                this.enableCheckstyle = loaded.enableCheckstyle;

                logger.info("Loaded config from: {}", configPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to load config, using defaults: {}", e.getMessage());
        }
    }

    public void save() {
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            objectMapper.writeValue(configPath.toFile(), this);
            logger.info("Saved config to: {}", configPath);
        } catch (Exception e) {
            logger.error("Failed to save config: {}", e.getMessage());
        }
    }

    private Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
    }

    // Getters and setters
    public int getWindowWidth() { return windowWidth; }
    public void setWindowWidth(int windowWidth) { this.windowWidth = windowWidth; }

    public int getWindowHeight() { return windowHeight; }
    public void setWindowHeight(int windowHeight) { this.windowHeight = windowHeight; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLastOpenedPath() { return lastOpenedPath; }
    public void setLastOpenedPath(String lastOpenedPath) { this.lastOpenedPath = lastOpenedPath; }

    public int getMaxConcurrentAnalyzers() { return maxConcurrentAnalyzers; }
    public void setMaxConcurrentAnalyzers(int maxConcurrentAnalyzers) {
        this.maxConcurrentAnalyzers = maxConcurrentAnalyzers;
    }

    public boolean isEnablePmd() { return enablePmd; }
    public void setEnablePmd(boolean enablePmd) { this.enablePmd = enablePmd; }

    public boolean isEnableSpotBugs() { return enableSpotBugs; }
    public void setEnableSpotBugs(boolean enableSpotBugs) { this.enableSpotBugs = enableSpotBugs; }

    public boolean isEnableCheckstyle() { return enableCheckstyle; }
    public void setEnableCheckstyle(boolean enableCheckstyle) { this.enableCheckstyle = enableCheckstyle; }
}
