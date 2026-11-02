package com.marketmind.agents;

import com.marketmind.behaviours.MarketAnalysisBehaviour;
import com.marketmind.services.MarketAnalysisService;
import jade.core.Agent;

public class MarketResearchAgent extends Agent {
    @Override protected void setup() {
        System.out.println("Research agent ready: " + getLocalName());
        MarketAnalysisService service = new MarketAnalysisService();
        addBehaviour(new MarketAnalysisBehaviour(this, "RESEARCH", service::analyze));
    }
}
