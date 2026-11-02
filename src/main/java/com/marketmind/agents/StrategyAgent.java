package com.marketmind.agents;

import com.marketmind.api.LLMClient;
import com.marketmind.config.ApiConfig;
import com.marketmind.models.AgentFinding;
import com.marketmind.models.AnalysisBundle;
import com.marketmind.utils.AgentConstants;
import com.marketmind.utils.MessageUtils;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.List;

public class StrategyAgent extends Agent {
    @Override protected void setup() {
        LLMClient llm = new LLMClient(new ApiConfig());
        addBehaviour(new CyclicBehaviour() {
            private final MessageTemplate filter = MessageTemplate.MatchOntology(AgentConstants.STRATEGY_REQUEST);
            @Override public void action() {
                ACLMessage request = myAgent.receive(filter);
                if (request == null) { block(); return; }
                try {
                    AnalysisBundle bundle = (AnalysisBundle) request.getContentObject();
                    StringBuilder evidence = new StringBuilder();
                    bundle.getFindings().forEach((role, finding) -> evidence.append(role).append(":\n")
                            .append(finding.getText()).append("\nWARNING: ")
                            .append(String.join("; ", finding.getWarnings())).append("\n"));
                    String strategy;
                    List<String> limitations;
                    if (llm.available()) {
                        try {
                            strategy = llm.proposeStrategy(bundle.getRequest().getDescription(), evidence.toString());
                            limitations = List.of("LLM-generated recommendations require independent verification.");
                        } catch (Exception ex) {
                            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
                            strategy = fallback();
                            limitations = List.of("LLM call failed: " + ex.getClass().getSimpleName());
                        }
                    } else {
                        strategy = fallback();
                        limitations = List.of("Local Ollama service is unavailable; strategy uses a rule-based checklist, not AI inference.");
                    }
                    AgentFinding finding = new AgentFinding("STRATEGY", strategy, List.of(), limitations);
                    myAgent.send(MessageUtils.reply(request, AgentConstants.FINDING, finding));
                } catch (Exception ex) {
                    System.err.println("Strategy processing error: " + ex);
                    ACLMessage failure = request.createReply();
                    failure.setPerformative(ACLMessage.FAILURE);
                    failure.setOntology(AgentConstants.FINDING);
                    failure.setContent("Strategy failed: " + ex.getClass().getSimpleName());
                    myAgent.send(failure);
                }
            }
        });
    }
    private String fallback() {
        return "Rule-based action plan (not an LLM market prediction):\n"
            + "1. Specify the exact target customer segment and problem to solve.\n"
            + "2. Verify at least five real competitors with dated sources.\n"
            + "3. Validate price and demand through small customer experiments.\n"
            + "4. Build unit economics from real supplier quotes, margins and expenses.\n"
            + "5. Review legal requirements with qualified local sources before launch.\n"
            + "6. Set measurable thresholds for a limited pilot and reassess.\n";
    }
}
