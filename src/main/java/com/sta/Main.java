package com.sta;

import com.sta.cli.CliRunner;
import com.sta.ui.MainWindow;
import com.formdev.flatlaf.FlatDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.swing.*;

/**
 * Main entry point for the Static Code Analyzer.
 * Supports both GUI and CLI modes.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Static Code Analyzer v2.0.0 starting...");

        // Check for CLI mode
        if (args.length > 0 && (args[0].equals("--cli") || args[0].equals("-c"))) {
            // Remove --cli flag and run CLI
            String[] cliArgs = new String[args.length - 1];
            System.arraycopy(args, 1, cliArgs, 0, cliArgs.length);
            int exitCode = new CommandLine(new CliRunner()).execute(cliArgs);
            System.exit(exitCode);
        } else if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            printUsage();
        } else {
            // Launch GUI
            launchGui();
        }
    }

    private static void launchGui() {
        try {
            // Set FlatLaf dark theme
            UIManager.setLookAndFeel(new FlatDarkLaf());
            
            // Enable anti-aliased text
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
        } catch (Exception e) {
            logger.warn("Failed to set FlatLaf look and feel", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }

    private static void printUsage() {
        System.out.println("Static Code Analyzer v2.0.0");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar sta.jar                    Launch GUI mode");
        System.out.println("  java -jar sta.jar --cli [options]    Run in CLI mode");
        System.out.println();
        System.out.println("CLI Options:");
        System.out.println("  -u, --url <URL>         GitHub repository URL");
        System.out.println("  -p, --path <PATH>       Local project path");
        System.out.println("  -f, --format <FORMAT>   Report format: html, json, csv");
        System.out.println("  -o, --output <FILE>     Output file path");
        System.out.println("  --fail-on <SEVERITY>    Fail on severity: CRITICAL, HIGH, MEDIUM, LOW");
        System.out.println("  --min-score <SCORE>     Minimum quality score (0-100)");
        System.out.println("  -q, --quiet             Suppress output except errors");
    }
}
