package com.marketmind.ui;

import com.marketmind.models.MarketRequest;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.Consumer;

/** Free-form prompt; country and budget are optional context, not sector presets. */
public class MarketRequestPanel extends JPanel {
    private final JTextArea prompt = new JTextArea(8, 60);
    private final JTextField country = new JTextField("MA", 8);
    private final JTextField budget = new JTextField("", 16);
    private final JTextField currency = new JTextField("MAD", 8);

    public MarketRequestPanel(Consumer<MarketRequest> submit) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        setBackground(MarketMindTheme.LIGHT);
        JLabel title = new JLabel("What business or market would you like to analyze?");
        title.setForeground(MarketMindTheme.INK);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19f));
        add(title, BorderLayout.NORTH);
        prompt.setBackground(MarketMindTheme.CARD);
        prompt.setForeground(MarketMindTheme.INK);
        prompt.setCaretColor(MarketMindTheme.INK);
        prompt.setLineWrap(true);
        prompt.setWrapStyleWord(true);
        prompt.setText("I want to launch a bookstore. Help me evaluate the market, competition, risks and a realistic pilot.");
        JScrollPane scroll = new JScrollPane(prompt);
        scroll.setBorder(BorderFactory.createTitledBorder("Your request — any sector, any business"));
        add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(MarketMindTheme.LIGHT);
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        fields.setBackground(MarketMindTheme.LIGHT);
        for (JTextField f : new JTextField[]{country, budget, currency}) {
            f.setBackground(MarketMindTheme.CARD);
            f.setForeground(MarketMindTheme.INK);
            f.setCaretColor(MarketMindTheme.INK);
        }
        fields.add(MarketMindTheme.text("Country (ISO):", 12, false, MarketMindTheme.MUTED)); fields.add(country);
        fields.add(MarketMindTheme.text("Budget (optional):", 12, false, MarketMindTheme.MUTED)); fields.add(budget);
        fields.add(MarketMindTheme.text("Currency:", 12, false, MarketMindTheme.MUTED)); fields.add(currency);
        bottom.add(fields, BorderLayout.CENTER);
        JButton run = MarketMindTheme.button("Analyze market", MarketMindTheme.ACCENT, MarketMindTheme.SIDEBAR);
        run.setFont(run.getFont().deriveFont(Font.BOLD, 14f));
        bottom.add(run, BorderLayout.EAST);
        JLabel hint = new JLabel("Use a 2/3-letter country code (MA, FR, USA...). With no API keys, results are limited to public indicators and checklists.");
        hint.setForeground(MarketMindTheme.MUTED);
        bottom.add(hint, BorderLayout.SOUTH);
        run.addActionListener(event -> {
            try {
                BigDecimal money = budget.getText().trim().isBlank() ? null : new BigDecimal(budget.getText().trim());
                MarketRequest request = new MarketRequest(prompt.getText(), country.getText().trim(),
                        money, currency.getText().trim());
                submit.accept(request);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Check the request", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(bottom, BorderLayout.SOUTH);
    }
}
