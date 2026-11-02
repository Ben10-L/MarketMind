package com.marketmind.models;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalysisBundle implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MarketRequest request;
    private final Map<String, AgentFinding> findings;
    private String strategy = "";

    public AnalysisBundle(MarketRequest request, Map<String, AgentFinding> findings) {
        this.request = request;
        this.findings = new LinkedHashMap<>(findings);
    }
    public MarketRequest getRequest() { return request; }
    public Map<String, AgentFinding> getFindings() { return findings; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
}
