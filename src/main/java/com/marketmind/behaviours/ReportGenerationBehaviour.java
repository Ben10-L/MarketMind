package com.marketmind.behaviours;

import com.marketmind.models.AnalysisBundle;
import com.marketmind.services.ReportService;
import com.marketmind.utils.AgentConstants;
import com.marketmind.utils.MessageUtils;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReportGenerationBehaviour extends CyclicBehaviour {
    private final ReportService service = new ReportService();
    private final MessageTemplate template = MessageTemplate.MatchOntology(AgentConstants.REPORT_REQUEST);
    public ReportGenerationBehaviour(Agent agent) { super(agent); }
    @Override public void action() {
        ACLMessage incoming = myAgent.receive(template);
        if (incoming == null) { block(); return; }
        try {
            AnalysisBundle bundle = (AnalysisBundle) incoming.getContentObject();
            myAgent.send(MessageUtils.reply(incoming, AgentConstants.REPORT_RESULT, service.save(bundle)));
        } catch (Exception ex) {
            ACLMessage failure = incoming.createReply();
            failure.setPerformative(ACLMessage.FAILURE);
            failure.setOntology(AgentConstants.REPORT_RESULT);
            failure.setContent("Report could not be generated: " + ex.getClass().getSimpleName());
            myAgent.send(failure);
        }
    }
}
