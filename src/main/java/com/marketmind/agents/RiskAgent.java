package com.marketmind.agents;

import com.marketmind.behaviours.RiskAssessmentBehaviour;
import com.marketmind.models.AgentFinding;
import com.marketmind.models.MarketRequest;
import jade.core.Agent;
import java.util.List;

public class RiskAgent extends Agent {
    @Override protected void setup() {
        addBehaviour(new RiskAssessmentBehaviour(this, this::analyze));
    }
    private AgentFinding analyze(MarketRequest request) {
        String text = "Risk CHECKLIST for the proposed business (items to investigate, not confirmed facts):\n"
            + "- Demand risk: interview buyers, test a landing page, and validate willingness to pay.\n"
            + "- Competitive risk: verify local alternatives, differentiation and pricing.\n"
            + "- Operating risk: calculate stock, supply chain, lead times and fixed costs.\n"
            + "- Compliance risk: consult current country/sector-specific legal and tax requirements.\n"
            + "- Data risk: country-level metrics may not describe a local customer segment.\n";
        return new AgentFinding("RISK", text, List.of(),
                List.of("This is a structured checklist, not a scored or verified risk assessment."));
    }
}
