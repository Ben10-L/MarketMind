package com.marketmind.behaviours;

import com.marketmind.models.AgentFinding;
import com.marketmind.models.MarketRequest;
import com.marketmind.utils.AgentConstants;
import com.marketmind.utils.MessageUtils;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.List;
import java.util.function.Function;

/** Generic JADE request handler used by the four data-analysis agents. */
public class MarketAnalysisBehaviour extends CyclicBehaviour {
    private final String role;
    private final Function<MarketRequest, AgentFinding> processor;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchOntology(AgentConstants.WORK_REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

    public MarketAnalysisBehaviour(Agent agent, String role, Function<MarketRequest, AgentFinding> processor) {
        super(agent);
        this.role = role;
        this.processor = processor;
    }
    @Override public void action() {
        ACLMessage request = myAgent.receive(template);
        if (request == null) { block(); return; }
        try {
            MarketRequest task = (MarketRequest) request.getContentObject();
            AgentFinding finding = processor.apply(task);
            myAgent.send(MessageUtils.reply(request, AgentConstants.FINDING, finding));
        } catch (Exception ex) {
            AgentFinding failure = new AgentFinding(role,
                    "No verified findings available for this stage.", List.of(),
                    List.of("Stage error: " + ex.getClass().getSimpleName()));
            try { myAgent.send(MessageUtils.reply(request, AgentConstants.FINDING, failure)); }
            catch (Exception nested) { System.err.println("Failed sending " + role + " reply: " + nested); }
        }
    }
}
