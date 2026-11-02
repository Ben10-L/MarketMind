package com.marketmind.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Editorial-style A4 report, generated locally with PDFBox.
 * Strict black / gray / white palette; automatic wrapping,
 * tables, source warnings, and page numbers. Dark pages use more printer ink.
 * ReportService is responsible for sanitizing LLM output before calling this class.
 */
public final class PdfReportExporter {
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float LEFT = 48;
    private static final float RIGHT = PAGE_W - LEFT;
    private static final float CONTENT_W = RIGHT - LEFT;
    // Match MarketMindTheme colors in the Swing user interface.
    private static final Color PAGE_BG = new Color(33, 33, 33);       // #212121
    private static final Color SIDEBAR = new Color(23, 23, 23);       // #171717
    private static final Color CARD = new Color(48, 48, 48);          // #303030
    private static final Color INK = new Color(236, 236, 236);        // #ECECEC
    private static final Color ACCENT = new Color(236, 236, 236);   // #ECECEC
    private static final Color HIGHLIGHT = new Color(215, 215, 215); // #D7D7D7
    private static final Color SUBTLE = new Color(56, 56, 56);
    private static final Color BODY = new Color(218, 218, 218);
    private static final Color MUTED = new Color(163, 163, 163);      // #A3A3A3
    private static final Color HAIRLINE = new Color(69, 69, 69);      // #454545
    private static final Color WARNING_BG = new Color(59, 59, 59);   // #3B3B3B
    private static final Color WARNING_TEXT = new Color(236, 236, 236);

    private PDDocument doc;
    private PDPageContentStream stream;
    private PDFont regular;
    private PDFont semibold;
    private float y;
    private int number;
    private int sectionNumber;

    public void export(String markdown, Path destination) throws IOException {
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (PDDocument d = new PDDocument()) {
            doc = d;
            regular = font(d, false);
            semibold = font(d, true);
            number = 0;
            sectionNumber = 0;
            startPage();
            String[] rows = (markdown == null ? "" : markdown).split("\\R", -1);
            for (int i = 0; i < rows.length; i++) {
                String line = rows[i].trim();
                if (line.isEmpty()) { y -= 4; continue; }
                if (line.equals("---")) { rule(); continue; }
                if (line.startsWith("# ")) { hero(line.substring(2)); continue; }
                if (line.startsWith("## ")) { section(line.substring(3)); continue; }
                if (line.startsWith("### ")) { subtitle(line.substring(4)); continue; }
                if (line.startsWith("|")) {
                    List<String> table = new ArrayList<>();
                    while (i < rows.length && rows[i].trim().startsWith("|")) {
                        table.add(rows[i].trim());
                        i++;
                    }
                    i--;
                    table(table);
                } else if (line.startsWith("- **Data limitation:**") || line.startsWith("- **Warning:**")) {
                    warning(strip(line.substring(2)));
                } else if (line.startsWith("- ")) {
                    bullet(strip(line.substring(2)));
                } else if (line.startsWith("**") && line.contains(":**")) {
                    metadata(strip(line));
                } else if (line.startsWith("http://") || line.startsWith("https://")) {
                    paragraph(line, 8.2f, HIGHLIGHT, 4);
                } else {
                    paragraph(strip(line), 9.5f, BODY, 7);
                }
            }
            finishPage();
            d.save(destination.toFile());
        } finally { doc = null; stream = null; }
    }

    private void startPage() throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        stream = new PDPageContentStream(doc, page);
        number++;
        // Full-bleed dark page background.
        fill(0, 0, PAGE_W, PAGE_H, PAGE_BG);
        // Header: minimal monogram, brand and section tag.
        fill(LEFT, PAGE_H - 52, 25, 25, ACCENT);
        write("M", semibold, 14, LEFT + 6, PAGE_H - 45, SIDEBAR);
        write("MARKETMIND", semibold, 11, LEFT + 35, PAGE_H - 43, INK);
        write("MARKET INTELLIGENCE", semibold, 7, RIGHT - 130, PAGE_H - 43, HIGHLIGHT);
        line(LEFT, PAGE_H - 66, RIGHT, PAGE_H - 66, HAIRLINE, .7f);
        y = PAGE_H - 88;
    }

    private void finishPage() throws IOException {
        line(LEFT, 48, RIGHT, 48, HAIRLINE, .7f);
        write("PRELIMINARY  /  VERIFY SOURCES BEFORE DECISIONS", regular, 7, LEFT, 34, MUTED);
        write(String.format(Locale.ROOT, "%02d", number), semibold, 8, RIGHT - 13, 34, HIGHLIGHT);
        stream.close();
    }

    private void ensure(float needed) throws IOException {
        if (y - needed < 70) { finishPage(); startPage(); }
    }

    private void hero(String title) throws IOException {
        float height = 138;
        ensure(height + 14);
        float bottom = y - height;
        fill(LEFT, bottom, CONTENT_W, height, SIDEBAR);
        fill(LEFT, bottom, 6, height, ACCENT);
        write("STRATEGIC RESEARCH  /  MARKETMIND AI", semibold, 8, LEFT + 23, y - 30, HIGHLIGHT);
        float cursor = y - 60;
        for (String row : wrap(strip(title), semibold, 19, CONTENT_W - 50)) {
            write(row, semibold, 19, LEFT + 23, cursor, Color.WHITE);
            cursor -= 24;
        }
        write("A clearer view of the opportunity - with limitations disclosed", regular, 8.5f,
                LEFT + 23, bottom + 19, MUTED);
        y = bottom - 20;
    }

    private void section(String title) throws IOException {
        ensure(52);
        y -= 9;
        sectionNumber++;
        fill(LEFT, y - 24, 4, 25, ACCENT);
        write(String.format(Locale.ROOT, "%02d", sectionNumber), semibold, 9, LEFT + 12, y - 14, HIGHLIGHT);
        write(strip(title).toUpperCase(Locale.ROOT), semibold, 12, LEFT + 37, y - 14, INK);
        y -= 40;
    }

    private void subtitle(String text) throws IOException {
        ensure(28);
        write(strip(text), semibold, 10, LEFT, y, INK);
        y -= 21;
    }

    private void metadata(String text) throws IOException {
        List<String> lines = wrap(text, regular, 9, CONTENT_W - 20);
        float height = Math.max(23, lines.size() * 13 + 10);
        ensure(height + 3);
        fill(LEFT, y - height + 8, CONTENT_W, height, CARD);
        float current = y - 8;
        for (String line : lines) {
            write(line, regular, 9, LEFT + 10, current, BODY);
            current -= 13;
        }
        y -= height + 5;
    }

    private void warning(String message) throws IOException {
        List<String> rows = wrap(message, regular, 8.9f, CONTENT_W - 26);
        float height = Math.max(28, rows.size() * 13 + 15);
        ensure(height + 7);
        fill(LEFT, y - height + 3, CONTENT_W, height, WARNING_BG);
        fill(LEFT, y - height + 3, 3, height, new Color(163, 163, 163));
        float yy = y - 11;
        for (String r : rows) { write(r, regular, 8.9f, LEFT + 13, yy, WARNING_TEXT); yy -= 13; }
        y -= height + 9;
    }

    private void bullet(String text) throws IOException {
        List<String> rows = wrap(text, regular, 9.3f, CONTENT_W - 22);
        ensure(rows.size() * 14 + 4);
        fill(LEFT + 2, y - 3, 4, 4, HIGHLIGHT);
        for (String row : rows) {
            write(row, regular, 9.3f, LEFT + 16, y, BODY);
            y -= 14;
        }
        y -= 4;
    }

    private void paragraph(String value, float size, Color color, float after) throws IOException {
        List<String> rows = wrap(value, regular, size, CONTENT_W);
        for (String row : rows) {
            ensure(14);
            write(row, regular, size, LEFT, y, color);
            y -= 14;
        }
        y -= after;
    }

    private void table(List<String> raw) throws IOException {
        if (raw.isEmpty()) return;
        List<String[]> lines = new ArrayList<>();
        for (String row : raw) {
            if (row.matches("\\|?[\\s|:-]+\\|?")) continue;
            String cleaned = row.replaceFirst("^\\|", "").replaceFirst("\\|$", "");
            String[] parts = cleaned.split("\\|", -1);
            for (int i = 0; i < parts.length; i++) parts[i] = strip(parts[i].trim());
            lines.add(parts);
        }
        if (lines.isEmpty()) return;
        int columns = lines.get(0).length;
        if (columns < 2 || columns > 5) {
            for (String[] cells : lines) paragraph(String.join("  |  ", cells), 8.5f, BODY, 3);
            return;
        }
        float cellW = CONTENT_W / columns;
        for (int index = 0; index < lines.size(); index++) {
            String[] cells = lines.get(index);
            List<List<String>> wrapped = new ArrayList<>();
            int maxLines = 1;
            for (int col = 0; col < columns; col++) {
                List<String> w = wrap(col < cells.length ? cells[col] : "", index == 0 ? semibold : regular,
                        8f, cellW - 15);
                wrapped.add(w);
                maxLines = Math.max(maxLines, w.size());
            }
            float height = Math.max(25, maxLines * 11 + 13);
            ensure(height + 2);
            fill(LEFT, y - height + 8, CONTENT_W, height, index == 0 ? SIDEBAR : (index % 2 == 1 ? CARD : SUBTLE));
            for (int col = 0; col < columns; col++) {
                float yy = y - 6;
                for (String txt : wrapped.get(col)) {
                    write(txt, index == 0 ? semibold : regular, 8, LEFT + col * cellW + 7, yy,
                            index == 0 ? HIGHLIGHT : BODY);
                    yy -= 11;
                }
            }
            y -= height;
        }
        y -= 12;
    }

    private void rule() throws IOException { ensure(17); line(LEFT, y - 2, RIGHT, y - 2, HAIRLINE, .8f); y -= 17; }
    private void fill(float x, float bottom, float width, float height, Color color) throws IOException {
        stream.setNonStrokingColor(color); stream.addRect(x, bottom, width, height); stream.fill();
    }
    private void line(float x1, float y1, float x2, float y2, Color color, float width) throws IOException {
        stream.setStrokingColor(color); stream.setLineWidth(width); stream.moveTo(x1, y1);
        stream.lineTo(x2, y2); stream.stroke();
    }
    private void write(String text, PDFont font, float size, float x, float baseline, Color color) throws IOException {
        stream.beginText(); stream.setNonStrokingColor(color); stream.setFont(font, size);
        stream.newLineAtOffset(x, baseline); stream.showText(safe(text, font)); stream.endText();
    }
    private static String strip(String value) {
        return value.replace("**", "").replace("__", "").replace("`", "")
                .replaceAll("\\[(.+?)]\\((https?://[^)]+)\\)", "$1 ($2)")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", " ").trim();
    }
    private List<String> wrap(String text, PDFont font, float size, float maximum) throws IOException {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String token : text.split("\\s+")) {
            if (token.isEmpty()) continue;
            String word = safe(token, font);
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (width(candidate, font, size) <= maximum) { line.setLength(0); line.append(candidate); continue; }
            if (!line.isEmpty()) { out.add(line.toString()); line.setLength(0); }
            for (int position = 0; position < word.length();) {
                int cp = word.codePointAt(position);
                String character = new String(Character.toChars(cp));
                if (!line.isEmpty() && width(line + character, font, size) > maximum) {
                    out.add(line.toString()); line.setLength(0);
                }
                line.append(character);
                position += Character.charCount(cp);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }
    private float width(String text, PDFont font, float size) throws IOException {
        return font.getStringWidth(text) * size / 1000f;
    }
    private String safe(String value, PDFont font) throws IOException {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length();) {
            int cp = value.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isISOControl(cp)) continue;
            String glyph = new String(Character.toChars(cp));
            try { font.encode(glyph); out.append(glyph); }
            catch (IllegalArgumentException exception) { out.append('?'); }
        }
        return out.toString();
    }
    private static PDFont font(PDDocument doc, boolean bold) throws IOException {
        String[] paths = bold ? new String[] {
                "/System/Library/Fonts/Supplemental/Arial Bold.ttf", "/Library/Fonts/Arial Bold.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                "C:\\Windows\\Fonts\\arialbd.ttf"
        } : new String[] {
                "/System/Library/Fonts/Supplemental/Arial.ttf", "/Library/Fonts/Arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "C:\\Windows\\Fonts\\arial.ttf"
        };
        for (String path : paths) {
            File file = new File(path);
            if (file.isFile()) try { return PDType0Font.load(doc, file); } catch (IOException ignored) { }
        }
        return new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD
                : Standard14Fonts.FontName.HELVETICA);
    }
}
