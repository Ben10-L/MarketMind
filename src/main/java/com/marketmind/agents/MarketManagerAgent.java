
package com.marketmind.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarketManagerAgent extends Agent {

    private static final String RESEARCHER_NAME = "researcher";
    private static final String ONTOLOGY = "marketmind-analysis";
    private static final long TIMEOUT_MS = 60000;

    // Stores the original request associated with each analysis
    private final Map<String, ACLMessage> pendingRequests =
            new HashMap<>();

    @Override
    protected void setup() {

        System.out.println(
                "[" + getLocalName() + "] MarketManager started."
        );

        // 1. Receive new requests
        addBehaviour(new CyclicBehaviour() {

            private final MessageTemplate template =
                    MessageTemplate.MatchPerformative(
                            ACLMessage.REQUEST
                    );

            @Override
            public void action() {

                ACLMessage message = myAgent.receive(template);

                if (message != null) {

                    System.out.println(
                            "[Manager] New request received."
                    );

                    sendResearchRequest(
                            message.getContent(),
                            message
                    );

                } else {
                    block();
                }
            }
        });

        // 2. Receive research results
        addBehaviour(new CyclicBehaviour() {

            private final MessageTemplate template =
                    MessageTemplate.and(
                            MessageTemplate.MatchOntology(ONTOLOGY),
                            MessageTemplate.or(
                                    MessageTemplate.MatchPerformative(
                                            ACLMessage.INFORM
                                    ),
                                    MessageTemplate.or(
                                            MessageTemplate.MatchPerformative(
                                                    ACLMessage.FAILURE
                                            ),
                                            MessageTemplate.MatchPerformative(
                                                    ACLMessage.REFUSE
                                            )
                                    )
                            )
                    );

            @Override
            public void action() {

                ACLMessage reply = myAgent.receive(template);

                if (reply != null) {
                    handleResearchResponse(reply);
                } else {
                    block();
                }
            }
        });

        // 3. Optional initial request from Main.java
        Object[] arguments = getArguments();

        if (arguments != null
                && arguments.length > 0
                && arguments[0] instanceof String) {

            String userRequest = (String) arguments[0];

            sendResearchRequest(userRequest, null);
        }
    }

    // Send a dynamic market research request
    private void sendResearchRequest(
            String userRequest,
            ACLMessage originalRequest
    ) {

        if (userRequest == null || userRequest.isBlank()) {

            System.out.println(
                    "[Manager] Invalid or empty request."
            );

            if (originalRequest != null) {
                sendError(
                        originalRequest,
                        ACLMessage.REFUSE,
                        "Market analysis request cannot be empty."
                );
            }

            return;
        }

        String conversationId =
                "marketmind-" + UUID.randomUUID();

        ACLMessage request =
                new ACLMessage(ACLMessage.REQUEST);

        AID researcher = new AID(
                RESEARCHER_NAME,
                AID.ISLOCALNAME
        );

        request.addReceiver(researcher);
        request.setContent(userRequest.trim());
        request.setConversationId(conversationId);
        request.setOntology(ONTOLOGY);

        // Remember who originally requested this analysis
        pendingRequests.put(
                conversationId,
                originalRequest
        );

        send(request);

        System.out.println(
                "[Manager] Request sent to researcher."
        );

        System.out.println(
                "[Manager] Task: " + userRequest
        );

        System.out.println(
                "[Manager] Conversation ID: " + conversationId
        );

        // Timeout for the research request
        addBehaviour(new WakerBehaviour(this, TIMEOUT_MS) {

            @Override
            protected void handleElapsedTimeout() {

                if (pendingRequests.containsKey(conversationId)) {

                    ACLMessage original =
                            pendingRequests.remove(conversationId);

                    System.out.println(
                            "[Manager] Research request timed out."
                    );

                    if (original != null) {
                        sendError(
                                original,
                                ACLMessage.FAILURE,
                                "Market research timed out."
                        );
                    }
                }
            }
        });
    }

    // Process the researcher's response
    private void handleResearchResponse(ACLMessage reply) {

        // Accept only responses from the research agent
        if (reply.getSender() == null
                || !RESEARCHER_NAME.equals(
                reply.getSender().getLocalName()
        )) {

            System.out.println(
                    "[Manager] Unexpected response sender."
            );
            return;
        }

        String conversationId = reply.getConversationId();

        if (conversationId == null
                || !pendingRequests.containsKey(conversationId)) {

            System.out.println(
                    "[Manager] Unknown or expired conversation."
            );
            return;
        }

        ACLMessage originalRequest =
                pendingRequests.remove(conversationId);

        System.out.println(
                "[Manager] Research response received."
        );

        System.out.println(
                "[Manager] Result: " + reply.getContent()
        );

        // Forward response to the original requester
        if (originalRequest != null) {

            ACLMessage response = originalRequest.createReply();

            response.setPerformative(reply.getPerformative());
            response.setContent(reply.getContent());

            send(response);

            System.out.println(
                    "[Manager] Response forwarded to requester."
            );
        }
    }

    // Return an error to the requesting agent
    private void sendError(
            ACLMessage originalRequest,
            int performative,
            String errorMessage
    ) {

        ACLMessage response = originalRequest.createReply();

        response.setPerformative(performative);
        response.setContent(errorMessage);

        send(response);
    }

    @Override
    protected void takeDown() {

        System.out.println(
                "[" + getLocalName() + "] MarketManager stopped."
        );
    }
}
