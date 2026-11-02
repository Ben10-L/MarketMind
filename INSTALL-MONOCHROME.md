# MarketMind AI — Black & White UI + PDF

## Files included in the patch

- `src/main/java/com/marketmind/ui/` — **all seven Swing Java classes**.
- `src/main/java/com/marketmind/services/PdfReportExporter.java` — monochrome PDF generation.

## Installation in your existing Git repository

1. Commit or back up your existing work before replacing files.
2. Extract `MarketMind-UI-and-PDF-Monochrome-Patch.zip`.
3. Copy the **entire** `src/main/java/com/marketmind/ui/` folder to the corresponding path of your existing IntelliJ project, replacing existing UI files.
4. Copy `src/main/java/com/marketmind/services/PdfReportExporter.java` into the existing services folder.
5. Preserve your existing `.git`, agent classes, API configuration and other working files.
6. Verify `pom.xml` has `org.apache.pdfbox:pdfbox:3.0.8` in `<dependencies>`.
7. Reload Maven; run `mvn clean test` and `mvn exec:java`.

## Shared palette (strictly grayscale)

| Use | Hex |
|-----|-----|
| Sidebar | `#171717` |
| Background / PDF page | `#212121` |
| Messages / input | `#303030` |
| Hover and warning | `#3B3B3B` |
| Borders | `#454545` |
| Main text / neutral accent | `#ECECEC` |
| Secondary text | `#A3A3A3` |

The chat-style interface does not add LLM conversation memory; each user submission creates a separate JADE market analysis. Dark PDF pages use more printer ink.
