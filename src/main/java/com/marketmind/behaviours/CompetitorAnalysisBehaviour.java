package com.marketmind.behaviours;

import com.marketmind.models.AgentFinding;
import com.marketmind.models.MarketRequest;
import jade.core.Agent;
import java.util.function.Function;

public class CompetitorAnalysisBehaviour extends MarketAnalysisBehaviour {
    public CompetitorAnalysisBehaviour(Agent agent, Function<MarketRequest, AgentFinding> processor) {
        super(agent, "COMPETITORS", processor);
    }
}
