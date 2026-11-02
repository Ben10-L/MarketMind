package com.marketmind.services;

import com.marketmind.models.AgentFinding;
import com.marketmind.models.AnalysisBundle;
import com.marketmind.models.AnalysisResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ReportService {
    public AnalysisResult save(AnalysisBundle bundle) throws IOException {
        StringBuilder md = new StringBuilder();
        var req = bundle.getRequest();
        md.append("# MarketMind — Market Intelligence Report\n\n")
          .append("**Created:** ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n")
          .append("**Request:** ").append(req.getDescription().replace("\n", " ")).append("\n\n")
          .append("**Country code:** ").append(req.getCountryCode()).append("\n\n")
          .append("**Budget:** ").append(req.getBudget() == null ? "Not provided" : req.getBudget() + " " + req.getCurrency()).append("\n\n")
          .append("**Request ID:** `").append(req.getId()).append("`\n\n");
        for (Map.Entry<String, AgentFinding> entry : bundle.getFindings().entrySet()) {
            AgentFinding finding = entry.getValue();
            md.append("## ").append(entry.getKey()).append("\n\n").append(finding.getText()).append("\n\n");
            for (String warning : finding.getWarnings())
                md.append("- **Data limitation:** ").append(warning).append("\n");
            md.append("\n");
        }
        String publishableStrategy = sanitizeStrategy(bundle.getStrategy());
        md.append("## Strategy\n\n").append(publishableStrategy).append("\n\n");
        Set<String> sources = new LinkedHashSet<>();
        bundle.getFindings().values().forEach(f -> sources.addAll(f.getSources()));
        md.append("## Data sources (verify access dates and freshness)\n\n");
        if (sources.isEmpty()) md.append("No online sources were successfully retrieved.\n");
        else for (String source : sources) md.append("- ").append(source).append("\n");
        md.append("\n---\nEducational market research aid; not professional financial or legal advice.\n");
        Path dir = Path.of("reports");
        Files.createDirectories(dir);
        Path path = dir.resolve("marketmind-" + req.getId() + ".md").toAbsolutePath();
        Files.writeString(path, md.toString(), StandardCharsets.UTF_8);
        Path pdfPath = dir.resolve("marketmind-" + req.getId() + ".pdf").toAbsolutePath();
        new PdfReportExporter().export(md.toString(), pdfPath);
        return new AnalysisResult(req.getId(), md.toString(), pdfPath.toString());
    }

    /** Never publish Ollama thinking traces as client-facing market strategy. */
    static String sanitizeStrategy(String raw) {
        if (raw == null || raw.isBlank()) return "Strategy unavailable; re-run the analysis.";
        String cleaned = raw.trim();
        // Some models return a closing tag without the opening tag.
        int close = cleaned.toLowerCase(java.util.Locale.ROOT).lastIndexOf("</think>");
        if (close >= 0) cleaned = cleaned.substring(close + "</think>".length()).trim();
        cleaned = cleaned.replaceAll("(?is)<think>.*?</think>", "").trim();
        if (cleaned.toLowerCase(java.util.Locale.ROOT).contains("<think>"))
            return "Strategy is incomplete: model returned unfinished reasoning. Re-run the analysis.";
        if (cleaned.isBlank()) return "Strategy unavailable: no publishable answer returned by the model.";
        // Reject a final half-written Markdown table separator, seen in local model output.
        String[] lines = cleaned.split("\\R");
        String last = lines[lines.length - 1].trim();
        if (last.startsWith("|") && last.replace("|", "").trim().matches("[-: ]+"))
            return "Strategy response was truncated. Re-run the analysis to obtain complete recommendations.";
        return cleaned;
    }
}
