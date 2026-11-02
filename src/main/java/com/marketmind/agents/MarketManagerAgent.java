package com.marketmind.agents;

import com.marketmind.models.AgentFinding;
import com.marketmind.models.AnalysisBundle;
import com.marketmind.models.AnalysisResult;
import com.marketmind.models.MarketRequest;
import com.marketmind.utils.AgentConstants;
import com.marketmind.utils.MessageUtils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Orchestrator for concurrently submitted JADE market analyses. */
public class MarketManagerAgent extends Agent {
    private static final String[] WORKERS = { AgentConstants.RESEARCHER, AgentConstants.COMPETITOR,
            AgentConstants.RISK, AgentConstants.FINANCIAL };
    private static final long TIMEOUT_MS = 150_000;
    private final Map<String, Job> active = new HashMap<>();

    private static class Job {
        final MarketRequest request;
        final long started = System.currentTimeMillis();
        final Map<String, AgentFinding> findings = new LinkedHashMap<>();
        boolean strategyStarted = false;
        Job(MarketRequest request) { this.request = request; }
    }

    @Override protected void setup() {
        System.out.println("Market manager running as " + getLocalName());
        addBehaviour(new CyclicBehaviour() {
            @Override public void action() {
                ACLMessage message = myAgent.receive();
                if (message == null) { block(); return; }
                try {
                    String ontology = message.getOntology();
                    if (AgentConstants.NEW_REQUEST.equals(ontology)) start((MarketRequest) message.getContentObject());
                    else if (AgentConstants.FINDING.equals(ontology)) handleFinding(message);
                    else if (AgentConstants.REPORT_RESULT.equals(ontology)) handleReport(message);
                } catch (Exception ex) {
                    notifyUI(message.getConversationId(), "Manager error: " + ex.getClass().getSimpleName());
                    System.err.println("Manager error: " + ex);
                }
            }
        });
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override protected void onTick() {
                long now = System.currentTimeMillis();
                for (String id : active.keySet().toArray(new String[0])) {
                    Job job = active.get(id);
                    if (job != null && now - job.started > TIMEOUT_MS) {
                        active.remove(id);
                        notifyUI(id, "Analysis timed out. Check API availability and try again.");
                    }
                }
            }
        });
        // Also supports CLI-started manager with a plain text first argument.
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof String text && !text.isBlank()) {
            try { start(new MarketRequest(text, "MA", null, "MAD")); }
            catch (Exception ex) { System.err.println("Startup request failed: " + ex); }
        }
    }

    private void start(MarketRequest request) throws Exception {
        if (active.containsKey(request.getId())) return;
        Job job = new Job(request);
        active.put(request.getId(), job);
        notifyUI(request.getId(), "Started analysis for " + request.getCountryCode());
        for (String worker : WORKERS) {
            send(MessageUtils.request(worker, AgentConstants.WORK_REQUEST, request.getId(), request));
            notifyUI(request.getId(), "Task dispatched to " + worker);
        }
    }

    private void handleFinding(ACLMessage message) throws Exception {
        String id = message.getConversationId();
        Job job = active.get(id);
        if (job == null) return; // late reply, expired job, or unknown ID
        String sender = message.getSender() == null ? "" : message.getSender().getLocalName();
        if (message.getPerformative() == ACLMessage.FAILURE) {
            notifyUI(id, sender + " failed: " + message.getContent());
            if (AgentConstants.STRATEGY.equals(sender)) {
                AnalysisBundle fallback = new AnalysisBundle(job.request, job.findings);
                fallback.setStrategy("Strategy stage unavailable. Inspect the verified findings and limitations above.");
                send(MessageUtils.request(AgentConstants.REPORT, AgentConstants.REPORT_REQUEST, id, fallback));
            }
            return;
        }
        if (!(message.getContentObject() instanceof AgentFinding finding)) return;
        if (AgentConstants.STRATEGY.equals(sender)) {
            if (!job.strategyStarted || !"STRATEGY".equals(finding.getRole())) return;
            AnalysisBundle bundle = new AnalysisBundle(job.request, job.findings);
            bundle.setStrategy(finding.getText() + "\n\nLimitations: " + String.join("; ", finding.getWarnings()));
            send(MessageUtils.request(AgentConstants.REPORT, AgentConstants.REPORT_REQUEST, id, bundle));
            notifyUI(id, "Strategy completed; report being generated");
            return;
        }
        boolean expected = false;
        for (String worker : WORKERS) if (worker.equals(sender)) expected = true;
        if (!expected || job.findings.containsKey(finding.getRole())) return;
        job.findings.put(finding.getRole(), finding);
        notifyUI(id, finding.getRole() + " completed (" + job.findings.size() + "/4)");
        if (job.findings.size() == WORKERS.length && !job.strategyStarted) {
            job.strategyStarted = true;
            send(MessageUtils.request(AgentConstants.STRATEGY, AgentConstants.STRATEGY_REQUEST,
                    id, new AnalysisBundle(job.request, job.findings)));
            notifyUI(id, "Strategy analysis started");
        }
    }

    private void handleReport(ACLMessage message) throws Exception {
        String id = message.getConversationId();
        if (!active.containsKey(id)) return;
        if (message.getSender() == null || !AgentConstants.REPORT.equals(message.getSender().getLocalName())) return;
        if (message.getPerformative() != ACLMessage.INFORM) {
            notifyUI(id, "Report error: " + message.getContent());
            active.remove(id);
            return;
        }
        AnalysisResult result = (AnalysisResult) message.getContentObject();
        active.remove(id);
        ACLMessage output = new ACLMessage(ACLMessage.INFORM);
        output.addReceiver(new AID(AgentConstants.UI, AID.ISLOCALNAME));
        output.setOntology(AgentConstants.UI_RESULT);
        output.setConversationId(id);
        output.setContentObject(result);
        send(output);
        notifyUI(id, "Report saved to " + result.getReportFile());
    }

    private void notifyUI(String id, String text) {
        ACLMessage event = new ACLMessage(ACLMessage.INFORM);
        event.addReceiver(new AID(AgentConstants.UI, AID.ISLOCALNAME));
        event.setOntology(AgentConstants.UI_STATUS);
        event.setConversationId(id == null ? "" : id);
        event.setContent(text);
        send(event);
        System.out.println("[Manager] " + text);
    }
}
