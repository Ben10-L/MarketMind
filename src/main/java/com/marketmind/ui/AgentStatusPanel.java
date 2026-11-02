package com.marketmind.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AgentStatusPanel extends JPanel {
    private final JTextArea log = new JTextArea();
    public AgentStatusPanel() {
        super(new BorderLayout());
        setBackground(MarketMindTheme.LIGHT);
        log.setBackground(MarketMindTheme.CARD);
        log.setForeground(MarketMindTheme.INK);
        log.setCaretColor(MarketMindTheme.INK);
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(new JScrollPane(log), BorderLayout.CENTER);
    }
    public void append(String id, String status) {
        String prefix = id == null || id.isBlank() ? "" : "[" + id.substring(0, Math.min(8, id.length())) + "] ";
        log.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "  " + prefix + status + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }
}
