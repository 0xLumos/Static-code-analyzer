package com.sta.report;

import com.sta.core.engine.AnalysisResult;
import com.sta.core.engine.Issue;
import com.sta.core.engine.Severity;
import com.sta.util.QualityScoreCalculator;
import com.sta.util.QualityScoreCalculator.QualityBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates self-contained HTML reports with embedded styling and charts.
 */
public class HtmlReportGenerator implements ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HtmlReportGenerator.class);

    @Override
    public String getFormat() {
        return "html";
    }

    @Override
    public void generate(AnalysisResult result, Path outputPath) throws IOException {
        String html = generateToString(result);
        Files.write(outputPath, html.getBytes(StandardCharsets.UTF_8));
        logger.info("HTML report generated: {}", outputPath);
    }

    @Override
    public String generateToString(AnalysisResult result) {
        QualityBreakdown breakdown = QualityScoreCalculator.calculateBreakdown(result);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>Code Analysis Report - ").append(escapeHtml(result.getProjectName())).append("</title>\n");
        sb.append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        appendStyles(sb);
        sb.append("</head>\n");
        sb.append("<body>\n");

        // Header
        sb.append("<header>\n");
        sb.append("  <div class=\"header-content\">\n");
        sb.append("    <h1>🔬 Code Analysis Report</h1>\n");
        sb.append("    <p class=\"subtitle\">").append(escapeHtml(result.getProjectName())).append("</p>\n");
        sb.append("  </div>\n");
        sb.append("  <div class=\"header-meta\">\n");
        String analysisTime = result.getAnalysisTime().atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("    <span>📅 ").append(analysisTime).append("</span>\n");
        if (result.getAnalysisDuration() != null) {
            sb.append("    <span>⏱️ ").append(result.getAnalysisDuration().getSeconds()).append("s</span>\n");
        }
        sb.append("  </div>\n");
        sb.append("</header>\n");

        // Summary Cards
        sb.append("<section class=\"summary-cards\">\n");
        appendSummaryCard(sb, "Quality Score", breakdown.grade(),
                String.format("%.1f / 100 - %s", breakdown.score(), breakdown.gradeDescription()),
                getGradeColorClass(breakdown.grade()));
        appendSummaryCard(sb, "Total Issues", String.valueOf(breakdown.totalIssues()),
                String.format("%.2f issues per KLOC", breakdown.issuesPerKLoc()), "blue");
        appendSummaryCard(sb, "Lines of Code", String.format("%,d", breakdown.linesOfCode()),
                result.getFileCount().size() + " file types analyzed", "green");
        appendSummaryCard(sb, "Critical + High",
                String.valueOf(breakdown.criticalCount() + breakdown.highCount()),
                "Require immediate attention", "red");
        sb.append("</section>\n");

        // Charts
        sb.append("<section class=\"charts\">\n");
        sb.append("  <div class=\"chart-container\">\n");
        sb.append("    <h3>Severity Distribution</h3>\n");
        sb.append("    <canvas id=\"severityChart\"></canvas>\n");
        sb.append("  </div>\n");
        sb.append("  <div class=\"chart-container\">\n");
        sb.append("    <h3>Top Violated Rules</h3>\n");
        sb.append("    <canvas id=\"rulesChart\"></canvas>\n");
        sb.append("  </div>\n");
        sb.append("</section>\n");

        // Issues Table
        sb.append("<section class=\"issues-section\">\n");
        sb.append("  <h2>📋 All Issues (").append(breakdown.totalIssues()).append(")</h2>\n");
        sb.append("  <input type=\"text\" id=\"searchBox\" placeholder=\"Search issues...\" onkeyup=\"filterTable()\">\n");
        sb.append("  <table id=\"issuesTable\">\n");
        sb.append("    <thead>\n");
        sb.append("      <tr>\n");
        sb.append("        <th>Severity</th>\n");
        sb.append("        <th>Rule</th>\n");
        sb.append("        <th>Message</th>\n");
        sb.append("        <th>File</th>\n");
        sb.append("        <th>Line</th>\n");
        sb.append("        <th>Analyzer</th>\n");
        sb.append("      </tr>\n");
        sb.append("    </thead>\n");
        sb.append("    <tbody>\n");

        for (Issue issue : result.getIssues()) {
            sb.append("      <tr class=\"severity-").append(issue.getSeverity().name().toLowerCase()).append("\">\n");
            sb.append("        <td><span class=\"severity-badge ").append(issue.getSeverity().name().toLowerCase())
              .append("\">").append(issue.getSeverity().getIcon()).append(" ")
              .append(issue.getSeverity().getDisplayName()).append("</span></td>\n");
            sb.append("        <td>").append(escapeHtml(issue.getRule())).append("</td>\n");
            sb.append("        <td>").append(escapeHtml(issue.getMessage())).append("</td>\n");
            sb.append("        <td>").append(escapeHtml(issue.getFileName())).append("</td>\n");
            sb.append("        <td>").append(issue.getStartLine()).append("</td>\n");
            sb.append("        <td>").append(escapeHtml(issue.getAnalyzer())).append("</td>\n");
            sb.append("      </tr>\n");
        }

        sb.append("    </tbody>\n");
        sb.append("  </table>\n");
        sb.append("</section>\n");

        // Chart Scripts
        appendChartScripts(sb, breakdown, result);

        // Filter Script
        sb.append("<script>\n");
        sb.append("function filterTable() {\n");
        sb.append("  var input = document.getElementById('searchBox').value.toLowerCase();\n");
        sb.append("  var rows = document.getElementById('issuesTable').getElementsByTagName('tr');\n");
        sb.append("  for (var i = 1; i < rows.length; i++) {\n");
        sb.append("    var text = rows[i].textContent.toLowerCase();\n");
        sb.append("    rows[i].style.display = text.indexOf(input) > -1 ? '' : 'none';\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("</script>\n");

        sb.append("<footer>\n");
        sb.append("  <p>Generated by Static Code Analyzer v2.0.0</p>\n");
        sb.append("</footer>\n");

        sb.append("</body>\n");
        sb.append("</html>");

        return sb.toString();
    }

    private void appendStyles(StringBuilder sb) {
        sb.append("<style>\n");
        sb.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
        sb.append("body { font-family: 'Segoe UI', sans-serif; background: #0F172A; color: #F1F5F9; line-height: 1.6; }\n");
        sb.append("header { background: #1E293B; padding: 2rem; display: flex; justify-content: space-between; align-items: center; }\n");
        sb.append(".header-content h1 { font-size: 1.75rem; color: #fff; }\n");
        sb.append(".subtitle { color: #94A3B8; font-size: 1rem; }\n");
        sb.append(".header-meta { display: flex; gap: 1.5rem; color: #64748B; font-size: 0.875rem; }\n");
        sb.append(".summary-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; padding: 2rem; }\n");
        sb.append(".card { background: #1E293B; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; }\n");
        sb.append(".card-title { color: #94A3B8; font-size: 0.875rem; margin-bottom: 0.5rem; }\n");
        sb.append(".card-value { font-size: 2.5rem; font-weight: 700; margin-bottom: 0.25rem; }\n");
        sb.append(".card-value.green { color: #22C55E; }\n");
        sb.append(".card-value.blue { color: #6366F1; }\n");
        sb.append(".card-value.red { color: #EF4444; }\n");
        sb.append(".card-value.yellow { color: #EAB308; }\n");
        sb.append(".card-subtitle { color: #64748B; font-size: 0.75rem; }\n");
        sb.append(".charts { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; padding: 0 2rem 2rem 2rem; }\n");
        sb.append(".chart-container { background: #1E293B; border: 1px solid #334155; border-radius: 12px; padding: 1.5rem; }\n");
        sb.append(".chart-container h3 { color: #F1F5F9; margin-bottom: 1rem; }\n");
        sb.append(".issues-section { padding: 0 2rem 2rem 2rem; }\n");
        sb.append(".issues-section h2 { color: #F1F5F9; margin-bottom: 1rem; }\n");
        sb.append("#searchBox { width: 100%; padding: 0.75rem 1rem; background: #1E293B; border: 1px solid #334155; border-radius: 8px; color: #F1F5F9; margin-bottom: 1rem; font-size: 1rem; }\n");
        sb.append("table { width: 100%; border-collapse: collapse; background: #1E293B; border-radius: 12px; overflow: hidden; }\n");
        sb.append("th { background: #334155; color: #F1F5F9; padding: 1rem; text-align: left; font-weight: 600; }\n");
        sb.append("td { padding: 0.75rem 1rem; border-bottom: 1px solid #334155; color: #CBD5E1; }\n");
        sb.append("tr:hover { background: #334155; }\n");
        sb.append(".severity-badge { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }\n");
        sb.append(".severity-badge.critical { background: #DC2626; color: #fff; }\n");
        sb.append(".severity-badge.high { background: #EA580C; color: #fff; }\n");
        sb.append(".severity-badge.medium { background: #CA8A04; color: #fff; }\n");
        sb.append(".severity-badge.low { background: #2563EB; color: #fff; }\n");
        sb.append(".severity-badge.info { background: #6B7280; color: #fff; }\n");
        sb.append("footer { text-align: center; padding: 2rem; color: #64748B; font-size: 0.875rem; }\n");
        sb.append("</style>\n");
    }

    private void appendSummaryCard(StringBuilder sb, String title, String value, String subtitle, String colorClass) {
        sb.append("  <div class=\"card\">\n");
        sb.append("    <div class=\"card-title\">").append(title).append("</div>\n");
        sb.append("    <div class=\"card-value ").append(colorClass).append("\">").append(value).append("</div>\n");
        sb.append("    <div class=\"card-subtitle\">").append(subtitle).append("</div>\n");
        sb.append("  </div>\n");
    }

    private void appendChartScripts(StringBuilder sb, QualityBreakdown breakdown, AnalysisResult result) {
        sb.append("<script>\n");

        // Severity Pie Chart
        sb.append("new Chart(document.getElementById('severityChart'), {\n");
        sb.append("  type: 'doughnut',\n");
        sb.append("  data: {\n");
        sb.append("    labels: ['Critical', 'High', 'Medium', 'Low', 'Info'],\n");
        sb.append("    datasets: [{\n");
        sb.append("      data: [").append(breakdown.criticalCount()).append(", ");
        sb.append(breakdown.highCount()).append(", ");
        sb.append(breakdown.mediumCount()).append(", ");
        sb.append(breakdown.lowCount()).append(", ");
        sb.append(breakdown.infoCount()).append("],\n");
        sb.append("      backgroundColor: ['#DC2626', '#EA580C', '#CA8A04', '#2563EB', '#6B7280']\n");
        sb.append("    }]\n");
        sb.append("  },\n");
        sb.append("  options: { plugins: { legend: { labels: { color: '#94A3B8' } } } }\n");
        sb.append("});\n");

        // Top Rules Bar Chart
        List<Map.Entry<String, Long>> topRules = result.getTopViolatedRules(8);
        sb.append("new Chart(document.getElementById('rulesChart'), {\n");
        sb.append("  type: 'bar',\n");
        sb.append("  data: {\n");
        sb.append("    labels: [");
        for (int i = 0; i < topRules.size(); i++) {
            if (i > 0) sb.append(", ");
            String rule = topRules.get(i).getKey();
            if (rule.length() > 20) rule = rule.substring(0, 17) + "...";
            sb.append("'").append(escapeJs(rule)).append("'");
        }
        sb.append("],\n");
        sb.append("    datasets: [{ data: [");
        for (int i = 0; i < topRules.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(topRules.get(i).getValue());
        }
        sb.append("], backgroundColor: '#6366F1' }]\n");
        sb.append("  },\n");
        sb.append("  options: { indexAxis: 'y', plugins: { legend: { display: false } }, scales: { x: { ticks: { color: '#94A3B8' } }, y: { ticks: { color: '#94A3B8' } } } }\n");
        sb.append("});\n");

        sb.append("</script>\n");
    }

    private String getGradeColorClass(String grade) {
        if (grade.startsWith("A")) return "green";
        if (grade.startsWith("B")) return "yellow";
        return "red";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    private String escapeJs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"");
    }
}
