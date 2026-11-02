# MarketMind — Monochrome interface and reports

The interface is ChatGPT-inspired, **not an official ChatGPT UI implementation**. The same black, white, and neutral-gray palette is used in the Swing UI and PDF exporter, without colored accents.

| Role | Color |
|------|-------|
| Sidebar / dark surface | `#171717` |
| Workspace / PDF page | `#212121` |
| Message bubbles / cards | `#303030` |
| Hover / warning surface | `#3B3B3B` |
| Borders | `#454545` |
| Main text / accent buttons | `#ECECEC` |
| Secondary text | `#A3A3A3` |
| Light highlight | `#D7D7D7` |

- Update `ui/MarketMindTheme.java` to customize the complete Swing palette.
- Update `services/PdfReportExporter.java` to customize the PDF palette.
- All seven Swing UI source files and PDF exporter are included in the UI patch.
- Each message starts an independent analysis, not a context-aware conversation; history in the sidebar is session-local.
- Dark-background PDFs are optimized for on-screen reading, not printing; printing will consume much more ink.
