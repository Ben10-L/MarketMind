package com.marketmind.services;

import com.marketmind.models.*;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {
    @Test void removesModelReasoningAndRejectsTruncatedTables() {
        String raw = "internal draft reasoning</think>\nUseful recommendations: verify demand.";
        assertEquals("Useful recommendations: verify demand.", ReportService.sanitizeStrategy(raw));
        assertTrue(ReportService.sanitizeStrategy("draft</think>\n| Heading | Gap |\n|---------")
                .contains("truncated"));
    }
    @Test void reportPreservesRequestAndSources() throws Exception {
        MarketRequest request = new MarketRequest("Test local agriculture", "MA", null, "MAD");
        AnalysisBundle bundle = new AnalysisBundle(request,
                Map.of("RESEARCH", new AgentFinding("RESEARCH", "Verified metric example", List.of("https://example.org/source"), List.of())));
        bundle.setStrategy("Verify demand first.");
        AnalysisResult result = new ReportService().save(bundle);
        try {
            assertTrue(result.getReport().contains("Test local agriculture"));
            assertTrue(result.getReport().contains("https://example.org/source"));
            assertTrue(Files.exists(Path.of(result.getReportFile())));
            assertTrue(result.getReportFile().endsWith(".pdf"));
            assertTrue(Files.size(Path.of(result.getReportFile())) > 200);
        } finally {
            Files.deleteIfExists(Path.of(result.getReportFile()));
            Files.deleteIfExists(Path.of(result.getReportFile().replaceAll("\\.pdf$", ".md")));
        }
    }
}
