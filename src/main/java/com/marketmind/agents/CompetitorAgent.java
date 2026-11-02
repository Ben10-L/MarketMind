package com.marketmind.agents;

import com.marketmind.api.SearchApiClient;
import com.marketmind.behaviours.CompetitorAnalysisBehaviour;
import com.marketmind.config.ApiConfig;
import com.marketmind.models.AgentFinding;
import com.marketmind.models.Competitor;
import com.marketmind.models.MarketRequest;
import com.marketmind.services.DataLoader;
import jade.core.Agent;
import java.util.ArrayList;
import java.util.List;

public class CompetitorAgent extends Agent {
    @Override protected void setup() {
        SearchApiClient api = new SearchApiClient(new ApiConfig());
        DataLoader local = new DataLoader();
        addBehaviour(new CompetitorAnalysisBehaviour(this, request -> analyze(api, local, request)));
    }

    private AgentFinding analyze(SearchApiClient api, DataLoader local, MarketRequest req) {
        List<Competitor> competitors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String basis;
        if (api.available()) {
            basis = "SearXNG search results (candidates, NOT verified competitors)";
            try {
                competitors.addAll(api.search(req.getDescription() + " " + req.getCountryCode() + " competitors businesses"));
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
                warnings.add("Search API failed: " + ex.getClass().getSimpleName());
            }
        } else {
            basis = "Manually curated local competitor records";
            warnings.add("SearXNG not running. Start the local search service or supply verified CSV records.");
        }
        if (competitors.isEmpty()) {
            try { competitors.addAll(local.localCompetitors(req.getCountryCode(), req.getDescription())); }
            catch (Exception ex) { warnings.add("Local CSV could not be loaded: " + ex.getClass().getSimpleName()); }
        }
        StringBuilder output = new StringBuilder(basis + ":\n");
        List<String> sources = new ArrayList<>();
        if (competitors.isEmpty()) output.append("No competitors verified or supplied. Add verified rows to data/raw/competitors.csv or enable search.\n");
        for (Competitor competitor : competitors) {
            output.append("- ").append(competitor.name()).append(": ").append(competitor.description())
                    .append(" — ").append(competitor.url()).append("\n");
            sources.add(competitor.url());
        }
        warnings.add("Search results are candidate leads only; independently verify operating status and competition relevance.");
        return new AgentFinding("COMPETITORS", output.toString(), sources, warnings);
    }
}
