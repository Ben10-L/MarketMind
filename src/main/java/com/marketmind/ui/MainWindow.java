package com.marketmind.ui;

import com.marketmind.controllers.MarketMindController;
import com.marketmind.models.AnalysisResult;
import com.marketmind.models.MarketRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BaseMultiResolutionImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Chat-inspired, single-window workspace for independent JADE market analyses.
 * History lives in this Swing session; it is NOT conversational memory for the LLM.
 * All JADE notifications are dispatched on Swing's Event Dispatch Thread.
 */
public final class MainWindow extends JFrame {
    private final MarketMindController controller;
    private final JPanel recentItems = new JPanel();
    private final JPanel transcript = new JPanel();
    private final JScrollPane transcriptScroll = new JScrollPane(transcript);
    private final JTextArea prompt = new JTextArea(3, 45);
    private final JTextField country = new JTextField("MA", 4);
    private final JTextField budget = new JTextField("", 10);
    private final JTextField currency = new JTextField("MAD", 5);
    private final JButton send = MarketMindTheme.button("Analyze  \u2191", MarketMindTheme.ACCENT, MarketMindTheme.SIDEBAR);
    private final JLabel headerTitle = MarketMindTheme.text("New market analysis", 15, true, MarketMindTheme.INK);
    private final JLabel headerStatus = MarketMindTheme.text("Local-first  /  JADE", 11, false, MarketMindTheme.MUTED);
    private final JTextArea activity = MarketMindTheme.paragraph("", 62, 22, 12, MarketMindTheme.INK);
    private final Map<String, AnalysisSession> sessions = new LinkedHashMap<>();
    private String activeId;
    private JButton newAnalysis;
    private int completed;

    private static final class AnalysisSession {
        final MarketRequest request;
        AnalysisResult result;
        String latest = "Request queued";
        AnalysisSession(MarketRequest request) { this.request = request; }
    }

    public MainWindow(MarketMindController controller) {
        super("MarketMind AI | Intelligence Workspace");
        this.controller = Objects.requireNonNull(controller);
        setIconImage(brandIcon("black", 40).getImage());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1290, 840);
        setLocationRelativeTo(null);
        setContentPane(layoutRoot());
        setVisible(false);
        installEvents();
        renderWelcome();
    }

    private static ImageIcon brandIcon(String variant, int size) {
        String path = "/ui/marketmind-" + variant + "-";
        Image standard = new ImageIcon(MainWindow.class.getResource(path + size + ".png")).getImage();
        Image highResolution = new ImageIcon(MainWindow.class.getResource(path + size * 2 + ".png")).getImage();
        return new ImageIcon(new BaseMultiResolutionImage(standard, highResolution));
    }

    private JPanel layoutRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MarketMindTheme.LIGHT);
        root.add(sidebar(), BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(MarketMindTheme.LIGHT);
        workspace.add(toolbar(), BorderLayout.NORTH);

        transcript.setLayout(new BoxLayout(transcript, BoxLayout.Y_AXIS));
        transcript.setBackground(MarketMindTheme.LIGHT);
        transcript.setBorder(new EmptyBorder(18, 24, 28, 24));
        transcriptScroll.setBorder(null);
        transcriptScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        transcriptScroll.getVerticalScrollBar().setUnitIncrement(17);
        transcriptScroll.getViewport().setBackground(MarketMindTheme.LIGHT);
        workspace.add(transcriptScroll, BorderLayout.CENTER);
        workspace.add(composer(), BorderLayout.SOUTH);
        root.add(workspace, BorderLayout.CENTER);
        return root;
    }

    private JComponent sidebar() {
        JPanel bar = new JPanel(new BorderLayout(0, 15));
        bar.setBackground(MarketMindTheme.SIDEBAR);
        bar.setPreferredSize(new Dimension(252, 0));
        bar.setBorder(new EmptyBorder(24, 16, 16, 16));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 3));
        brand.setOpaque(false);
        JLabel mark = new JLabel(brandIcon("white", 32));
        brand.add(mark);
        brand.add(MarketMindTheme.text("MarketMind AI", 17, true, Color.WHITE));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(brand);
        top.add(Box.createVerticalStrut(23));
        newAnalysis = MarketMindTheme.button("+  New analysis", MarketMindTheme.CARD, Color.WHITE);
        newAnalysis.setHorizontalAlignment(SwingConstants.LEFT);
        newAnalysis.setMaximumSize(new Dimension(225, 44));
        newAnalysis.setAlignmentX(Component.LEFT_ALIGNMENT);
        newAnalysis.addActionListener(event -> resetWorkspace());
        top.add(newAnalysis);
        top.add(Box.createVerticalStrut(27));
        JLabel label = MarketMindTheme.text("RECENT ANALYSES", 10, true, MarketMindTheme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(label);
        top.add(Box.createVerticalStrut(13));
        bar.add(top, BorderLayout.NORTH);

        recentItems.setLayout(new BoxLayout(recentItems, BoxLayout.Y_AXIS));
        recentItems.setOpaque(false);
        JScrollPane scroll = new JScrollPane(recentItems);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        bar.add(scroll, BorderLayout.CENTER);

        JPanel foot = new JPanel();
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(12, 4, 4, 4));
        JLabel tech = MarketMindTheme.text("JADE  /  OLLAMA  /  SEARXNG", 10, true, MarketMindTheme.HIGHLIGHT);
        tech.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(tech);
        foot.add(Box.createVerticalStrut(8));
        JLabel note = MarketMindTheme.text("Free local tools  ·  Source-aware", 11, false, MarketMindTheme.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(note);
        bar.add(foot, BorderLayout.SOUTH);
        return bar;
    }

    private JComponent toolbar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MarketMindTheme.LIGHT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MarketMindTheme.BORDER),
                new EmptyBorder(16, 31, 16, 27)));
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        headerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(headerTitle);
        titles.add(Box.createVerticalStrut(5));
        titles.add(headerStatus);
        header.add(titles, BorderLayout.WEST);
        JButton logs = MarketMindTheme.button("Agent activity", MarketMindTheme.CARD, MarketMindTheme.HIGHLIGHT);
        logs.addActionListener(e -> showActivity());
        header.add(logs, BorderLayout.EAST);
        return header;
    }

    private JComponent composer() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(MarketMindTheme.LIGHT);
        wrapper.setBorder(new EmptyBorder(10, 35, 17, 35));
        MarketMindTheme.Surface card = new MarketMindTheme.Surface(new BorderLayout(0, 9),
                MarketMindTheme.CARD, MarketMindTheme.BORDER, 22);
        card.setBorder(new EmptyBorder(17, 19, 15, 19));
        prompt.setLineWrap(true);
        prompt.setWrapStyleWord(true);
        prompt.setFont(MarketMindTheme.font(14, false));
        prompt.setForeground(MarketMindTheme.INK);
        prompt.setCaretColor(MarketMindTheme.INK);
        prompt.setBackground(MarketMindTheme.CARD);
        prompt.setBorder(new EmptyBorder(0, 0, 2, 0));
        prompt.setText("");
        JScrollPane textScroll = new JScrollPane(prompt);
        textScroll.setBorder(null);
        textScroll.getViewport().setBackground(MarketMindTheme.CARD);
        textScroll.setPreferredSize(new Dimension(500, 74));
        card.add(textScroll, BorderLayout.CENTER);
        JLabel placeholder = MarketMindTheme.text("Describe your business idea or ask a market question...", 12, false,
                MarketMindTheme.MUTED);
        card.add(placeholder, BorderLayout.NORTH);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        fields.setOpaque(false);
        fields.add(fieldLabel("Country"));
        fieldStyle(country, 49);
        fields.add(country);
        fields.add(Box.createHorizontalStrut(7));
        fields.add(fieldLabel("Budget"));
        fieldStyle(budget, 106);
        fields.add(budget);
        fields.add(fieldLabel("Currency"));
        fieldStyle(currency, 58);
        fields.add(currency);

        JPanel actions = new JPanel(new BorderLayout(6, 0));
        actions.setOpaque(false);
        actions.add(fields, BorderLayout.CENTER);
        actions.add(send, BorderLayout.EAST);
        card.add(actions, BorderLayout.SOUTH);
        wrapper.add(card, BorderLayout.CENTER);
        JLabel help = MarketMindTheme.text("Each message starts an independent analysis  ·  Ctrl/Cmd + Enter to send  ·  Confirm critical figures with sources",
                11, false, MarketMindTheme.MUTED);
        help.setHorizontalAlignment(SwingConstants.CENTER);
        help.setBorder(new EmptyBorder(10, 0, 0, 0));
        wrapper.add(help, BorderLayout.SOUTH);
        return wrapper;
    }

    private JLabel fieldLabel(String label) { return MarketMindTheme.text(label, 11, false, MarketMindTheme.MUTED); }
    private void fieldStyle(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, 29));
        field.setFont(MarketMindTheme.font(12, false));
        field.setBackground(MarketMindTheme.CARD);
        field.setForeground(MarketMindTheme.INK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MarketMindTheme.BORDER),
                new EmptyBorder(4, 7, 4, 7)));
    }

    private void installEvents() {
        send.addActionListener(event -> submit());
        AbstractAction sendShortcut = new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { submit(); }
        };
        prompt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "submitAnalysis");
        prompt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK), "submitAnalysis");
        prompt.getActionMap().put("submitAnalysis", sendShortcut);
        controller.addListener(new MarketMindController.Listener() {
            @Override public void onStatus(String requestId, String message) {
                SwingUtilities.invokeLater(() -> {
                    activity.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                            "  " + (requestId == null ? "" : requestId.substring(0, Math.min(8, requestId.length()))) +
                            "  " + message + "\n");
                    AnalysisSession session = sessions.get(requestId);
                    if (session != null) {
                        session.latest = message;
                        if (requestId.equals(activeId)) renderSession(session);
                    }
                });
            }
            @Override public void onResult(AnalysisResult result) {
                SwingUtilities.invokeLater(() -> {
                    AnalysisSession session = sessions.get(result.getRequestId());
                    if (session != null) {
                        session.result = result;
                        completed++;
                        session.latest = "Analysis complete - PDF ready";
                        refreshHistory();
                        if (result.getRequestId().equals(activeId)) renderSession(session);
                    }
                });
            }
        });
    }

    private void submit() {
        String requestText = prompt.getText().trim();
        try {
            BigDecimal value = budget.getText().trim().isEmpty() ? null : new BigDecimal(budget.getText().trim());
            MarketRequest request = new MarketRequest(requestText, country.getText(), value, currency.getText());
            AnalysisSession session = new AnalysisSession(request);
            sessions.put(request.getId(), session);
            activeId = request.getId();
            prompt.setText("");
            controller.submit(request);
            refreshHistory();
            renderSession(session);
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Check your request", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetWorkspace() {
        activeId = null;
        headerTitle.setText("New market analysis");
        headerStatus.setText("Local-first  /  JADE");
        renderWelcome();
        prompt.setText("");
        refreshHistory();
        prompt.requestFocusInWindow();
    }

    private void refreshHistory() {
        recentItems.removeAll();
        AnalysisSession[] items = sessions.values().toArray(new AnalysisSession[0]);
        for (int i = items.length - 1; i >= 0; i--) {
            AnalysisSession s = items[i];
            String text = s.request.getDescription().replace('\n', ' ');
            if (text.length() > 27) text = text.substring(0, 27) + "...";
            JButton item = MarketMindTheme.button(text,
                    s.request.getId().equals(activeId) ? MarketMindTheme.HOVER : MarketMindTheme.SIDEBAR,
                    MarketMindTheme.SIDEBAR_TEXT);
            item.setToolTipText(s.request.getDescription());
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setFont(MarketMindTheme.font(12, false));
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setMaximumSize(new Dimension(220, 39));
            item.addActionListener(event -> {
                activeId = s.request.getId();
                refreshHistory();
                renderSession(s);
            });
            recentItems.add(item);
            recentItems.add(Box.createVerticalStrut(4));
        }
        recentItems.revalidate();
        recentItems.repaint();
    }

    private void renderWelcome() {
        transcript.removeAll();
        transcript.add(Box.createVerticalStrut(98));
        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel(brandIcon("white", 40));
        JLabel title = MarketMindTheme.text("What market are we exploring?", 26, true, MarketMindTheme.INK);
        JLabel sub = MarketMindTheme.text("Ask about any business idea. MarketMind will coordinate research, risks and strategy.",
                13, false, MarketMindTheme.MUTED);
        for (JLabel label : new JLabel[]{icon, title, sub}) {
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            welcome.add(label);
            welcome.add(Box.createVerticalStrut(14));
        }
        welcome.add(Box.createVerticalStrut(30));
        JPanel suggestions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        suggestions.setOpaque(false);
        suggestion(suggestions, "Evaluate a bookstore", "I want to launch a bookstore. Evaluate local demand, competition, risks and a realistic pilot.");
        suggestion(suggestions, "Explore food delivery", "Analyze the opportunity for a food delivery business, competitors, operational risks and costs.");
        suggestion(suggestions, "Study online education", "Assess the online education market, possible customer segments and a low-cost pilot.");
        welcome.add(suggestions);
        transcript.add(center(welcome));
        refreshView();
    }

    private void suggestion(JPanel group, String label, String text) {
        JButton chip = MarketMindTheme.button(label, MarketMindTheme.CARD, MarketMindTheme.INK);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MarketMindTheme.BORDER), new EmptyBorder(13, 14, 13, 14)));
        chip.addActionListener(e -> { prompt.setText(text); prompt.requestFocusInWindow(); });
        group.add(chip);
    }

    private void renderSession(AnalysisSession session) {
        transcript.removeAll();
        headerTitle.setText("Market analysis");
        headerStatus.setText(session.result == null ? "Analysis in progress  /  " + session.request.getCountryCode()
                : "Completed  /  PDF available");
        transcript.add(center(messageCard("YOU", session.request.getDescription(), false, session.request)));
        transcript.add(Box.createVerticalStrut(16));
        if (session.result == null) {
            transcript.add(center(messageCard("MARKETMIND", "Our JADE agents are working on your request.\n\n" +
                    "Latest activity: " + session.latest + "\n\n" +
                    "Research  ·  Competition  ·  Risk  ·  Finance  ·  Strategy  ·  Report",
                    true, session.request)));
        } else {
            transcript.add(center(resultCard(session)));
        }
        transcript.add(Box.createVerticalStrut(18));
        refreshView();
    }

    private JComponent messageCard(String title, String body, boolean assistant, MarketRequest request) {
        MarketMindTheme.Surface card = new MarketMindTheme.Surface(new BorderLayout(0, 11),
                assistant ? MarketMindTheme.LIGHT : MarketMindTheme.CARD,
                assistant ? null : MarketMindTheme.BORDER, 18);
        card.setBorder(new EmptyBorder(19, 20, 21, 20));
        card.setPreferredSize(new Dimension(720, assistant ? 211 : Math.max(115, Math.min(245, 85 + body.length() / 90 * 16))));
        JLabel heading = MarketMindTheme.text(title, 10, true, assistant ? MarketMindTheme.ACCENT : MarketMindTheme.MUTED);
        card.add(heading, BorderLayout.NORTH);
        JTextArea text = MarketMindTheme.paragraph(body, 58, 5, 14, MarketMindTheme.INK);
        text.setBorder(new EmptyBorder(0, 1, 0, 1));
        card.add(text, BorderLayout.CENTER);
        if (!assistant) {
            String context = request.getCountryCode() + (request.getBudget() == null ? "" :
                    "  ·  " + request.getBudget() + " " + request.getCurrency());
            card.add(MarketMindTheme.text(context, 11, false, MarketMindTheme.MUTED), BorderLayout.SOUTH);
        }
        return card;
    }

    private JComponent resultCard(AnalysisSession session) {
        MarketMindTheme.Surface card = new MarketMindTheme.Surface(new BorderLayout(0, 12),
                MarketMindTheme.CARD, MarketMindTheme.BORDER, 18);
        card.setBorder(new EmptyBorder(20, 21, 21, 21));
        card.setPreferredSize(new Dimension(720, 460));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(MarketMindTheme.text("MARKETMIND  /  REPORT READY", 10, true, MarketMindTheme.ACCENT));
        title.add(Box.createVerticalStrut(8));
        title.add(MarketMindTheme.text("Your market intelligence brief", 20, true, MarketMindTheme.INK));
        top.add(title, BorderLayout.WEST);
        card.add(top, BorderLayout.NORTH);

        String summary = extractStrategy(session.result.getReport());
        JTextArea description = MarketMindTheme.paragraph(summary, 61, 10, 13, MarketMindTheme.INK);
        description.setBorder(new EmptyBorder(9, 0, 6, 0));
        JScrollPane preview = new JScrollPane(description);
        preview.setBorder(null);
        preview.getViewport().setBackground(MarketMindTheme.CARD);
        card.add(preview, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        JButton open = MarketMindTheme.button("Open PDF report", MarketMindTheme.ACCENT, MarketMindTheme.SIDEBAR);
        open.addActionListener(e -> openReport(session.result));
        JButton copy = MarketMindTheme.button("Copy report text", MarketMindTheme.HOVER, MarketMindTheme.HIGHLIGHT);
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(session.result.getReport()), null));
        buttons.add(open); buttons.add(copy);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private static String extractStrategy(String markdown) {
        String lower = markdown.toLowerCase(java.util.Locale.ROOT);
        int start = lower.indexOf("## strategy");
        if (start < 0) return "Analysis complete. Open the PDF for the market indicators, risk assessment and recommendations.";
        String strategy = markdown.substring(start + "## strategy".length()).trim();
        int next = strategy.indexOf("\n## ");
        if (next >= 0) strategy = strategy.substring(0, next);
        strategy = strategy.replace("**", "").replace("#", "").replace("`", "").trim();
        return strategy.length() > 2200 ? strategy.substring(0, 2200).trim() + "\n\nRead the complete report in PDF." : strategy;
    }

    private void openReport(AnalysisResult result) {
        try {
            File file = new File(result.getReportFile());
            if (!file.isFile()) throw new IllegalStateException("Report file does not exist: " + file);
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to open PDF: " + e.getMessage(),
                    "Open report", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent center(JComponent component) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        container.setOpaque(false);
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(component);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(70, component.getPreferredSize().height + 7)));
        return container;
    }

    private void refreshView() {
        transcript.revalidate();
        transcript.repaint();
        SwingUtilities.invokeLater(() -> transcriptScroll.getVerticalScrollBar().setValue(0));
    }

    private void showActivity() {
        JTextArea area = new JTextArea(activity.getText(), 23, 73);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBackground(MarketMindTheme.LIGHT);
        area.setForeground(MarketMindTheme.INK);
        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(760, 470));
        JOptionPane.showMessageDialog(this, pane, "JADE Agent Activity", JOptionPane.PLAIN_MESSAGE);
    }
}
