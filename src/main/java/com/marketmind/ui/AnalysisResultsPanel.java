package com.marketmind.ui;

import com.marketmind.models.AnalysisResult;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;

public class AnalysisResultsPanel extends JPanel {
    private final JTextArea content = new JTextArea();
    private AnalysisResult current;
    public AnalysisResultsPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setBackground(MarketMindTheme.LIGHT);
        content.setBackground(MarketMindTheme.CARD);
        content.setForeground(MarketMindTheme.INK);
        content.setCaretColor(MarketMindTheme.INK);
        content.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setEditable(false);
        content.setText("No report yet. Submit an analysis in the New Analysis tab.");
        add(new JScrollPane(content), BorderLayout.CENTER);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setBackground(MarketMindTheme.LIGHT);
        JButton copy = MarketMindTheme.button("Copy report", MarketMindTheme.CARD, MarketMindTheme.INK);
        copy.addActionListener(e -> {
            if (current != null) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(current.getReport()), null);
        });
        JButton open = MarketMindTheme.button("Open PDF report", MarketMindTheme.ACCENT, MarketMindTheme.SIDEBAR);
        open.addActionListener(e -> {
            if (current == null) return;
            try { Desktop.getDesktop().open(new File(current.getReportFile())); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Could not open: " + ex.getMessage()); }
        });
        toolbar.add(copy); toolbar.add(open);
        add(toolbar, BorderLayout.NORTH);
    }
    public void display(AnalysisResult result) {
        current = result;
        content.setText(result.getReport());
        content.setCaretPosition(0);
    }
}
