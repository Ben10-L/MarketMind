package com.marketmind.services;

import com.marketmind.api.WorldBankClient;
import com.marketmind.config.ApiConfig;
import com.marketmind.models.AgentFinding;
import com.marketmind.models.MarketData;
import com.marketmind.models.MarketRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MarketAnalysisService {
    private final ApiConfig config = new ApiConfig();
    private final WorldBankClient worldBank = new WorldBankClient();

    public AgentFinding analyze(MarketRequest request) {
        List<String> urls = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        StringBuilder out = new StringBuilder("Country: " + request.getCountryCode() + "\n");
        if (!config.useWorldBank()) {
            warnings.add("World Bank API is disabled in config.properties.");
        } else {
            String[][] indicators = {
                {"SP.POP.TOTL", "Population", "people"},
                {"FP.CPI.TOTL.ZG", "Consumer price inflation", "% annual"},
                {"NY.GDP.MKTP.CD", "GDP", "current US$"}
            };
            for (String[] indicator : indicators) {
                try {
                    Optional<MarketData> metric = worldBank.latest(request.getCountryCode(), indicator[0], indicator[1], indicator[2]);
                    if (metric.isPresent()) {
                        MarketData data = metric.get();
                        out.append("- ").append(data.indicator()).append(" (").append(data.year()).append("): ")
                           .append(String.format(Locale.US, "%,.2f", data.value())).append(" ").append(data.unit()).append("\n");
                        urls.add(data.sourceUrl());
                    } else warnings.add("No recent World Bank value for " + indicator[1]);
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
                    warnings.add("World Bank " + indicator[1] + " unavailable: " + ex.getClass().getSimpleName());
                }
            }
        }
        out.append("These are country-wide indicators, not estimates of demand for the user's specific business.\n");
        return new AgentFinding("RESEARCH", out.toString(), urls, warnings);
    }
}
