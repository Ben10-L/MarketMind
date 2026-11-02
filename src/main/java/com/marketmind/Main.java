package com.marketmind;

import com.marketmind.agents.*;
import com.marketmind.controllers.MarketMindController;
import com.marketmind.ui.MainWindow;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        MarketMindController controller = new MarketMindController();
        try {
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "false");
            // Create a private, local JADE platform. No TCP exposure is needed by the app GUI.
            AgentContainer container = Runtime.instance().createMainContainer(profile);
            create(container, "manager", MarketManagerAgent.class, null);
            create(container, "researcher", MarketResearchAgent.class, null);
            create(container, "competitor", CompetitorAgent.class, null);
            create(container, "risk", RiskAgent.class, null);
            create(container, "financial", FinancialAgent.class, null);
            create(container, "strategy", StrategyAgent.class, null);
            create(container, "report", ReportAgent.class, null);
            create(container, "ui", UiGatewayAgent.class, new Object[]{controller});
            SwingUtilities.invokeLater(() -> new MainWindow(controller).setVisible(true));
        } catch (Exception e) {
            System.err.println("MarketMind could not start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static void create(AgentContainer container, String id, Class<?> type, Object[] arguments)
            throws StaleProxyException {
        container.createNewAgent(id, type.getName(), arguments).start();
    }
}
