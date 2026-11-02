package com.marketmind.ui;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final JLabel total = new JLabel("Analyses submitted: 0");
    private final JLabel finished = new JLabel("Reports completed: 0");
    private final JLabel last = new JLabel("Ready to analyze your first business idea.");
    private int submissions = 0;
    private int reports = 0;
    public DashboardPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(MarketMindTheme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(26,26,26,26));
        JLabel brand = new JLabel("MarketMind AI | JADE Intelligence Console");
        brand.setForeground(MarketMindTheme.INK);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 23f));
        add(brand); add(Box.createVerticalStrut(25));
        for (JLabel label : new JLabel[]{total, finished, last}) {
            label.setForeground(MarketMindTheme.INK);
            label.setFont(label.getFont().deriveFont(15f));
            add(label); add(Box.createVerticalStrut(16));
        }
        JTextArea note = new JTextArea("Pipeline: GUI → UI Gateway → Manager → [Research, Competitor, Risk, Financial] "
                + "→ Strategy → Report.\n\nReports are saved under reports/. API keys are optional. "
                + "With no keys, the strategy is explicitly rule-based and competitors remain unverified/unavailable.");
        note.setForeground(MarketMindTheme.MUTED);
        note.setWrapStyleWord(true); note.setLineWrap(true); note.setOpaque(false); note.setEditable(false);
        note.setFont(note.getFont().deriveFont(14f));
        add(note);
    }
    public void submitted() { total.setText("Analyses submitted: " + (++submissions)); }
    public void completed() { finished.setText("Reports completed: " + (++reports)); }
    public void status(String msg) { last.setText("Latest: " + (msg.length() > 85 ? msg.substring(0, 85) + "…" : msg)); }
}
