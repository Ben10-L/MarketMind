package com.marketmind.ui;

import com.marketmind.models.FinancialAnalysis;
import com.marketmind.models.MarketRequest;
import com.marketmind.services.FinancialService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

/** An illustrative budget allocation chart; NOT a demand or profit forecast. */
public class ChartsPanel extends JPanel {
    private MarketRequest request;
    private FinancialAnalysis analysis;
    public ChartsPanel() {
        setBackground(MarketMindTheme.LIGHT);
        setPreferredSize(new Dimension(740, 470));
    }
    public void setRequest(MarketRequest request) {
        this.request = request;
        this.analysis = new FinancialService().allocate(request);
        repaint();
    }
    @Override protected void paintComponent(Graphics raw) {
        super.paintComponent(raw);
        Graphics2D g = (Graphics2D) raw.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(MarketMindTheme.INK);
            g.setFont(getFont().deriveFont(Font.BOLD, 18f));
            g.drawString("Illustrative budget distribution (not a forecast)", 25, 45);
            if (analysis == null || analysis.illustrativeAllocation().isEmpty()) {
                g.setFont(getFont().deriveFont(14f));
                g.drawString("Enter a budget to display an illustrative allocation.", 25, 85);
                return;
            }
            int top = 95;
            BigDecimal budget = request.getBudget();
            int maxWidth = Math.max(60, getWidth() - 380);
            for (Map.Entry<String, BigDecimal> item : analysis.illustrativeAllocation().entrySet()) {
                g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
                g.setColor(MarketMindTheme.INK);
                g.drawString(item.getKey(), 25, top + 17);
                int bar = (int) (maxWidth * item.getValue().doubleValue() / budget.doubleValue());
                g.setColor(MarketMindTheme.ACCENT);
                g.fillRoundRect(245, top + 3, Math.max(1, bar), 23, 6, 6);
                g.setColor(MarketMindTheme.INK);
                g.drawString(item.getValue().toPlainString() + " " + request.getCurrency(),
                        Math.min(255 + bar, getWidth() - 120), top + 20);
                top += 57;
            }
            g.setColor(MarketMindTheme.MUTED);
            g.drawString("Template ratios are assumptions; validate real costs with suppliers.", 25, top + 25);
        } finally { g.dispose(); }
    }
}
