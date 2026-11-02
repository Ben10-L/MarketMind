package com.marketmind.agents;

import com.marketmind.behaviours.ReportGenerationBehaviour;
import jade.core.Agent;

public class ReportAgent extends Agent {
    @Override protected void setup() { addBehaviour(new ReportGenerationBehaviour(this)); }
}
