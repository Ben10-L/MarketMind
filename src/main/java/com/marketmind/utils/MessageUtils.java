package com.marketmind.utils;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.io.Serializable;

public final class MessageUtils {
    private MessageUtils() {}
    public static ACLMessage request(String recipient, String ontology, String id, Serializable payload)
            throws IOException {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(recipient, AID.ISLOCALNAME));
        message.setOntology(ontology);
        message.setConversationId(id);
        message.setContentObject(payload);
        return message;
    }
    public static ACLMessage reply(ACLMessage incoming, String ontology, Serializable value)
            throws IOException {
        ACLMessage message = incoming.createReply();
        message.setPerformative(ACLMessage.INFORM);
        message.setOntology(ontology);
        message.setContentObject(value);
        return message;
    }
}
