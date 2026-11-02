package com.marketmind.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketmind.config.ApiConfig;
import com.marketmind.models.Competitor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Searches a self-hosted SearXNG instance using its free JSON API; no API key. */
public class SearchApiClient {
    private final ApiConfig config;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public SearchApiClient(ApiConfig config) { this.config = config; }

    /** Local availability check. /search?format=json must also be enabled in SearXNG. */
    public boolean available() {
        try {
            URI root = URI.create(config.searxngUrl().replaceAll("/+$", "") + "/");
            HttpRequest req = HttpRequest.newBuilder(root).timeout(Duration.ofSeconds(2)).GET().build();
            HttpResponse<Void> response = http.send(req, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        }
    }

    public List<Competitor> search(String query) throws IOException, InterruptedException {
        if (query == null || query.isBlank()) return List.of();
        String base = config.searxngUrl().replaceAll("/+$", "");
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI url = URI.create(base + "/search?q=" + q + "&format=json&categories=general");
        if (!"http".equalsIgnoreCase(url.getScheme())
                && !"https".equalsIgnoreCase(url.getScheme())) {
            throw new IOException("SEARXNG_URL must use HTTP or HTTPS");
        }
        HttpRequest request = HttpRequest.newBuilder(url)
                .timeout(Duration.ofSeconds(22))
                .header("Accept", "application/json").GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("SearXNG returned HTTP " + response.statusCode()
                    + " (ensure JSON is enabled in settings.yml)");
        }
        JsonNode items = mapper.readTree(response.body()).path("results");
        List<Competitor> results = new ArrayList<>();
        if (items.isArray()) {
            for (JsonNode item : items) {
                String title = shorten(item.path("title").asText(""), 160);
                String description = shorten(item.path("content").asText(""), 350);
                String link = item.path("url").asText("");
                if (!title.isBlank() && (link.startsWith("https://") || link.startsWith("http://"))) {
                    results.add(new Competitor(title, description, link));
                }
                if (results.size() >= config.searchLimit()) break;
            }
        }
        return results;
    }

    private String shorten(String value, int limit) {
        String cleaned = value.replaceAll("[\r\n\t]+", " ").trim();
        return cleaned.length() <= limit ? cleaned : cleaned.substring(0, limit) + "…";
    }
}
