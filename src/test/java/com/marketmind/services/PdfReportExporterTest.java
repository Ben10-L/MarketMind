package com.marketmind.services;

import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PdfReportExporterTest {
    @Test void styledReportWithTableAndWarningsIsReadable() throws Exception {
        Path temp = Files.createTempFile("marketmind-style-", ".pdf");
        try {
            String markdown = "# MarketMind — Market Intelligence Report\n\n" +
                    "**Request:** Evaluate a community bookstore\n\n" +
                    "## Financial\n\n" +
                    "| Category | Cost |\n|---|---|\n| Research | 2000 MAD |\n\n" +
                    "- **Data limitation:** Figures are illustrative only.\n\n" +
                    "## Strategy\n\nValidate demand before investing.";
            new PdfReportExporter().export(markdown, temp);
            assertTrue(Files.size(temp) > 500);
            try (var pdf = Loader.loadPDF(temp.toFile())) {
                assertTrue(pdf.getNumberOfPages() > 0);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
