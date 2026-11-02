package com.marketmind.agents;

import com.marketmind.behaviours.MarketAnalysisBehaviour;
import com.marketmind.models.AgentFinding;
import com.marketmind.models.FinancialAnalysis;
import com.marketmind.models.MarketRequest;
import com.marketmind.services.FinancialService;
import jade.core.Agent;
import java.util.List;

public class FinancialAgent extends Agent {
    @Override protected void setup() {
        FinancialService service = new FinancialService();
        addBehaviour(new MarketAnalysisBehaviour(this, "FINANCIAL", req -> analyze(service, req)));
    }
    private AgentFinding analyze(FinancialService service, MarketRequest request) {
        FinancialAnalysis analysis = service.allocate(request);
        StringBuilder text = new StringBuilder("Budget: ");
        text.append(request.getBudget() == null ? "not provided" : request.getBudget() + " " + request.getCurrency());
        text.append("\n");
        analysis.illustrativeAllocation().forEach((label, amount) ->
                text.append("- ").append(label).append(": ").append(amount).append(" ").append(request.getCurrency()).append("\n"));
        text.append(analysis.note()).append("\n");
        return new AgentFinding("FINANCIAL", text.toString(), List.of(),
                List.of("Allocation ratios are arbitrary illustrative assumptions, not evidence of profitability."));
    }
}
