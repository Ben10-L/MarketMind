package com.marketmind.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketmind.models.MarketData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Year;
import java.util.Optional;

public class WorldBankClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public Optional<MarketData> latest(String country, String indicator, String name, String unit)
            throws IOException, InterruptedException {
        if (!country.matches("[A-Z]{2,3}") || !indicator.matches("[A-Z0-9.]+"))
            throw new IllegalArgumentException("Unexpected country or indicator");
        int year = Year.now().getValue();
        String url = "https://api.worldbank.org/v2/country/" + country.toLowerCase()
                + "/indicator/" + indicator + "?format=json&per_page=100&date=" + (year - 8) + ":" + year;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(9))
                .header("Accept", "application/json").GET().build();
        HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("World Bank status " + response.statusCode());
        JsonNode root = mapper.readTree(response.body());
        if (!root.isArray() || root.size() < 2 || !root.get(1).isArray()) return Optional.empty();
        // The endpoint normally returns recent years first. We still select by maximum date.
        MarketData latest = null;
        for (JsonNode item : root.get(1)) {
            JsonNode v = item.path("value");
            if (!v.isNumber()) continue;
            String observationYear = item.path("date").asText("");
            if (latest == null || observationYear.compareTo(latest.year()) > 0)
                latest = new MarketData(name, observationYear, v.asDouble(), unit, url);
        }
        return Optional.ofNullable(latest);
    }
}
