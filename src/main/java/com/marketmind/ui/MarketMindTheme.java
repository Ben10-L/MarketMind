package com.marketmind.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Small Swing styling system; no external look-and-feel dependency required. */
public final class MarketMindTheme {
    private MarketMindTheme() {}
    // Strict grayscale palette shared by all Swing views (no color accents).
    public static final Color SIDEBAR = new Color(23, 23, 23);      // #171717
    public static final Color LIGHT = new Color(33, 33, 33);        // #212121
    public static final Color CARD = new Color(48, 48, 48);         // #303030
    public static final Color HOVER = new Color(59, 59, 59);        // #3B3B3B
    public static final Color BORDER = new Color(69, 69, 69);       // #454545
    public static final Color INK = new Color(236, 236, 236);       // #ECECEC
    public static final Color MUTED = new Color(163, 163, 163);     // #A3A3A3
    public static final Color SIDEBAR_TEXT = INK;
    public static final Color ACCENT = new Color(236, 236, 236);     // #ECECEC: high-contrast actions
    public static final Color HIGHLIGHT = new Color(215, 215, 215);  // #D7D7D7: neutral emphasis
    public static final Color USER_BUBBLE = CARD;                    // #303030: user message
    public static final Color WHITE = Color.WHITE;

    public static Font font(float points, boolean bold) {
        return new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, Math.round(points));
    }
    public static JLabel text(String content, float size, boolean bold, Color color) {
        JLabel label = new JLabel(content);
        label.setForeground(color);
        label.setFont(font(size, bold));
        return label;
    }
    public static JButton button(String label, Color background, Color foreground) {
        JButton button = new JButton(label);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        button.setFont(font(13, true));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setRolloverEnabled(true);
        return button;
    }
    public static JTextArea paragraph(String text, int cols, int rows, float size, Color foreground) {
        JTextArea area = new JTextArea(text, rows, cols);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFocusable(false);
        area.setForeground(foreground);
        area.setFont(font(size, false));
        area.setBorder(null);
        return area;
    }
    public static final class Surface extends JPanel {
        private final Color color;
        private final Color stroke;
        private final int radius;
        public Surface(LayoutManager layout, Color color, Color stroke, int radius) {
            super(layout);
            this.color = color;
            this.stroke = stroke;
            this.radius = radius;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, radius, radius);
                if (stroke != null) {
                    g.setColor(stroke);
                    g.setStroke(new BasicStroke(1));
                    g.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, radius, radius);
                }
            } finally { g.dispose(); }
            super.paintComponent(graphics);
        }
    }
}
