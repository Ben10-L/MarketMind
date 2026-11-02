package com.marketmind.models;

import java.io.Serializable;
import java.util.List;

public class AgentFinding implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String role;
    private final String text;
    private final List<String> sources;
    private final List<String> warnings;

    public AgentFinding(String role, String text, List<String> sources, List<String> warnings) {
        this.role = role;
        this.text = text;
        this.sources = List.copyOf(sources);
        this.warnings = List.copyOf(warnings);
    }
    public String getRole() { return role; }
    public String getText() { return text; }
    public List<String> getSources() { return sources; }
    public List<String> getWarnings() { return warnings; }
}
