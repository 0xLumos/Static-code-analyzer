package com.sta.ui;

import com.sta.config.AppConfig;
import com.sta.core.engine.AnalysisEngine;
import com.sta.core.engine.AnalysisResult;
import com.sta.core.engine.Issue;
import com.sta.core.engine.Severity;
import com.sta.report.HtmlReportGenerator;
import com.sta.util.QualityScoreCalculator;
import com.sta.util.QualityScoreCalculator.QualityBreakdown;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main application window with modern FlatLaf styling.
 */
public class MainWindow extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private final AnalysisEngine engine;
    private final AppConfig config;

    // UI Components
    private JTextField sourceInput;
    private JButton analyzeButton;
    private JButton exportButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JPanel contentPanel;
    private JTable issuesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // State
    private AnalysisResult currentResult;

    public MainWindow() {
        this.engine = new AnalysisEngine();
        this.config = AppConfig.getInstance();

        initializeUI();
        showWelcomePanel();
    }

    private void initializeUI() {
        setTitle("Static Code Analyzer v2.0.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(config.getWindowWidth(), config.getWindowHeight());
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout());

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(15, 23, 42));
        add(contentPanel, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Static Code Analyzer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);


        titlePanel.add(titleLabel);

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        inputPanel.setOpaque(false);

        sourceInput = new JTextField(35);
        sourceInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sourceInput.putClientProperty("JTextField.placeholderText", "Enter GitHub URL or local path...");
        sourceInput.addActionListener(e -> startAnalysis());

        analyzeButton = new JButton("🔍 Analyze");
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        analyzeButton.setFocusPainted(false);
        analyzeButton.addActionListener(e -> startAnalysis());

        exportButton = new JButton("📊 Export");
        exportButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exportButton.setFocusPainted(false);
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportReport());

        inputPanel.add(sourceInput);
        inputPanel.add(analyzeButton);
        inputPanel.add(exportButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(inputPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(15, 0));
        statusBar.setBackground(new Color(30, 41, 59));
        statusBar.setBorder(new EmptyBorder(8, 20, 8, 20));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(148, 163, 184));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(200, 16));
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        JLabel versionLabel = new JLabel("v2.0.0");
        versionLabel.setForeground(new Color(100, 116, 139));
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(statusLabel);
        leftPanel.add(progressBar);

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void showWelcomePanel() {
        contentPanel.removeAll();

        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(new Color(15, 23, 42));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcomeTitle = new JLabel("🔬 Welcome to Static Code Analyzer");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeTitle.setForeground(Color.WHITE);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeText = new JLabel("<html><center>Analyze your Java projects for code quality issues, " +
                "potential bugs, and style violations using PMD, SpotBugs, and Checkstyle.</center></html>");
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeText.setForeground(new Color(148, 163, 184));
        welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeText.setHorizontalAlignment(SwingConstants.CENTER);

        // Quick start cards
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.add(createQuickStartCard("🌐", "GitHub Repository",
                "Enter a public GitHub URL", "https://github.com/spring-projects/spring-petclinic"));
        cardsPanel.add(createQuickStartCard("📁", "Local Project",
                "Analyze local project", "C:\\Projects\\my-java-project"));

        content.add(welcomeTitle);
        content.add(Box.createVerticalStrut(15));
        content.add(welcomeText);
        content.add(Box.createVerticalStrut(40));
        content.add(cardsPanel);

        welcomePanel.add(content);
        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createQuickStartCard(String icon, String title, String description, String example) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(250, 150));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(148, 163, 184));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descLabel);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                sourceInput.setText(example);
                sourceInput.requestFocus();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(99, 102, 241), 1),
                        new EmptyBorder(20, 20, 20, 20)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                        new EmptyBorder(20, 20, 20, 20)
                ));
            }
        });

        return card;
    }

    private void startAnalysis() {
        String source = sourceInput.getText().trim();
        if (source.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a GitHub URL or local path",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Disable controls
        analyzeButton.setEnabled(false);
        exportButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);

        showAnalyzingPanel();

        // Configure callbacks
        engine.onStatus(status -> SwingUtilities.invokeLater(() -> statusLabel.setText(status)));
        engine.onProgress(progress -> SwingUtilities.invokeLater(() ->
                progressBar.setValue((int) (progress * 100))));

        // Run analysis async
        CompletableFuture.supplyAsync(() -> engine.analyze(source).join())
                .thenAccept(result -> SwingUtilities.invokeLater(() -> showResults(result)))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        showError(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                        analyzeButton.setEnabled(true);
                        progressBar.setVisible(false);
                    });
                    return null;
                });
    }

    private void showAnalyzingPanel() {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 42));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel spinnerLabel = new JLabel("⏳");
        spinnerLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        spinnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel analyzingLabel = new JLabel("Analyzing code...");
        analyzingLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        analyzingLabel.setForeground(Color.WHITE);
        analyzingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tipLabel = new JLabel("💡 Tip: Analysis includes PMD, Checkstyle, and SpotBugs checks");
        tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tipLabel.setForeground(new Color(148, 163, 184));
        tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(spinnerLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(analyzingLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(tipLabel);

        panel.add(content);
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showResults(AnalysisResult result) {
        this.currentResult = result;
        contentPanel.removeAll();

        QualityBreakdown breakdown = QualityScoreCalculator.calculateBreakdown(result);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(new Color(15, 23, 42));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Summary cards row
        mainPanel.add(createSummaryCardsPanel(result, breakdown), BorderLayout.NORTH);

        // Split pane for charts and table
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(new Color(15, 23, 42));
        splitPane.setBorder(null);
        splitPane.setDividerLocation(350);

        // Charts panel
        splitPane.setTopComponent(createChartsPanel(result, breakdown));

        // Issues table
        splitPane.setBottomComponent(createIssuesPanel(result));

        mainPanel.add(splitPane, BorderLayout.CENTER);

        contentPanel.add(mainPanel, BorderLayout.CENTER);

        // Re-enable controls
        analyzeButton.setEnabled(true);
        exportButton.setEnabled(true);
        progressBar.setVisible(false);
        statusLabel.setText("Analysis complete - " + result.getTotalIssueCount() + " issues found");

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createSummaryCardsPanel(AnalysisResult result, QualityBreakdown breakdown) {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);

        // Score card
        JPanel scoreCard = createCard("Quality Score", breakdown.grade(),
                String.format("%.1f / 100", breakdown.score()), getGradeColor(breakdown.grade()));

        // Total Issues card
        JPanel issuesCard = createCard("Total Issues", String.valueOf(breakdown.totalIssues()),
                String.format("%.2f per KLOC", breakdown.issuesPerKLoc()), new Color(99, 102, 241));

        // Lines of Code card
        JPanel locCard = createCard("Lines of Code", String.format("%,d", breakdown.linesOfCode()),
                result.getFileCount().size() + " file types", new Color(34, 197, 94));

        // Critical Issues card
        long criticalHigh = breakdown.criticalCount() + breakdown.highCount();
        JPanel criticalCard = createCard("Critical + High",
                String.valueOf(criticalHigh),
                "Require attention", new Color(239, 68, 68));

        panel.add(scoreCard);
        panel.add(issuesCard);
        panel.add(locCard);
        panel.add(criticalCard);

        return panel;
    }

    private JPanel createCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(148, 163, 184));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createChartsPanel(AnalysisResult result, QualityBreakdown breakdown) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);

        // Severity Pie Chart
        panel.add(createPieChartPanel(breakdown));

        // Top Rules Bar Chart
        panel.add(createBarChartPanel(result));

        return panel;
    }

    private JPanel createPieChartPanel(QualityBreakdown breakdown) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (breakdown.criticalCount() > 0) dataset.setValue("Critical", breakdown.criticalCount());
        if (breakdown.highCount() > 0) dataset.setValue("High", breakdown.highCount());
        if (breakdown.mediumCount() > 0) dataset.setValue("Medium", breakdown.mediumCount());
        if (breakdown.lowCount() > 0) dataset.setValue("Low", breakdown.lowCount());
        if (breakdown.infoCount() > 0) dataset.setValue("Info", breakdown.infoCount());

        JFreeChart chart = ChartFactory.createPieChart(
                "Severity Distribution", dataset, true, true, false);

        chart.setBackgroundPaint(new Color(30, 41, 59));
        chart.getTitle().setPaint(Color.WHITE);
        chart.getLegend().setBackgroundPaint(new Color(30, 41, 59));
        chart.getLegend().setItemPaint(new Color(148, 163, 184));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(30, 41, 59));
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null);
        plot.setSectionPaint("Critical", new Color(220, 38, 38));
        plot.setSectionPaint("High", new Color(234, 88, 12));
        plot.setSectionPaint("Medium", new Color(202, 138, 4));
        plot.setSectionPaint("Low", new Color(37, 99, 235));
        plot.setSectionPaint("Info", new Color(107, 114, 128));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(new Color(30, 41, 59));
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        return chartPanel;
    }

    private JPanel createBarChartPanel(AnalysisResult result) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Map.Entry<String, Long>> topRules = result.getTopViolatedRules(8);
        for (Map.Entry<String, Long> entry : topRules) {
            String rule = entry.getKey();
            if (rule.length() > 20) rule = rule.substring(0, 17) + "...";
            dataset.addValue(entry.getValue(), "Count", rule);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top Violated Rules", null, "Count",
                dataset, PlotOrientation.HORIZONTAL, false, true, false);

        chart.setBackgroundPaint(new Color(30, 41, 59));
        chart.getTitle().setPaint(Color.WHITE);

        chart.getCategoryPlot().setBackgroundPaint(new Color(30, 41, 59));
        chart.getCategoryPlot().setOutlineVisible(false);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(99, 102, 241));
        chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(new Color(148, 163, 184));
        chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(new Color(148, 163, 184));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(new Color(30, 41, 59));
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        return chartPanel;
    }

    private JPanel createIssuesPanel(AnalysisResult result) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Header with filter
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🔍 Issues (" + result.getTotalIssueCount() + ")");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search issues...");

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchField, BorderLayout.EAST);

        // Table
        String[] columns = {"Severity", "Rule", "Message", "File", "Line", "Analyzer"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Issue issue : result.getIssues()) {
            tableModel.addRow(new Object[]{
                    issue.getSeverity().getIcon() + " " + issue.getSeverity().getDisplayName(),
                    issue.getRule(),
                    issue.getMessage(),
                    issue.getFileName(),
                    issue.getStartLine(),
                    issue.getAnalyzer()
            });
        }

        issuesTable = new JTable(tableModel);
        issuesTable.setFillsViewportHeight(true);
        issuesTable.setRowHeight(28);
        issuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        issuesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        issuesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        issuesTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        issuesTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        issuesTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        issuesTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        sorter = new TableRowSorter<>(tableModel);
        issuesTable.setRowSorter(sorter);

        // Search filtering
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(issuesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void showError(String message) {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 42));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel iconLabel = new JLabel("❌");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel errorLabel = new JLabel("Analysis Failed");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        errorLabel.setForeground(new Color(239, 68, 68));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(148, 163, 184));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton retryButton = new JButton("Try Again");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(e -> showWelcomePanel());

        content.add(iconLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(errorLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(messageLabel);
        content.add(Box.createVerticalStrut(20));
        content.add(retryButton);

        panel.add(content);
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void exportReport() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No analysis results to export",
                    "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(currentResult.getProjectName() + "-report.html"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path outputPath = chooser.getSelectedFile().toPath();
                HtmlReportGenerator generator = new HtmlReportGenerator();
                generator.generate(currentResult, outputPath);
                statusLabel.setText("Report exported: " + outputPath);
                JOptionPane.showMessageDialog(this, "Report saved to " + outputPath,
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to export report: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Color getGradeColor(String grade) {
        if (grade.startsWith("A")) return new Color(34, 197, 94);
        if (grade.startsWith("B")) return new Color(132, 204, 22);
        if (grade.startsWith("C")) return new Color(234, 179, 8);
        if (grade.startsWith("D")) return new Color(239, 68, 68);
        return new Color(220, 38, 38);
    }
}
