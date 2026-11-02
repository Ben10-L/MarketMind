package com.marketmind.agents;

import com.marketmind.controllers.MarketMindController;
import com.marketmind.models.AnalysisResult;
import com.marketmind.models.MarketRequest;
import com.marketmind.utils.AgentConstants;
import com.marketmind.utils.MessageUtils;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class UiGatewayAgent extends Agent {
    @Override protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length == 0 || !(args[0] instanceof MarketMindController controller)) {
            System.err.println("UI Gateway requires MarketMindController argument");
            doDelete();
            return;
        }
        addBehaviour(new TickerBehaviour(this, 150) {
            @Override protected void onTick() {
                MarketRequest request;
                while ((request = controller.nextRequest()) != null) {
                    try {
                        myAgent.send(MessageUtils.request(AgentConstants.MANAGER,
                                AgentConstants.NEW_REQUEST, request.getId(), request));
                    } catch (Exception ex) {
                        controller.status(request.getId(), "Could not send request: " + ex.getMessage());
                    }
                }
            }
        });
        addBehaviour(new CyclicBehaviour() {
            @Override public void action() {
                ACLMessage message = myAgent.receive();
                if (message == null) { block(); return; }
                try {
                    if (AgentConstants.UI_STATUS.equals(message.getOntology()))
                        controller.status(message.getConversationId(), message.getContent());
                    else if (AgentConstants.UI_RESULT.equals(message.getOntology()))
                        controller.result((AnalysisResult) message.getContentObject());
                } catch (Exception ex) { System.err.println("UI Gateway message error: " + ex); }
            }
        });
    }
}
