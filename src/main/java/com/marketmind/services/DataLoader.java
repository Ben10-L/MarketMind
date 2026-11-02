package com.marketmind.services;

import com.marketmind.models.Competitor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Optional manually verified dataset: country,sector,name,description,url */
public class DataLoader {
    public List<Competitor> localCompetitors(String country, String sector) throws IOException {
        Path path = Path.of("data", "raw", "competitors.csv");
        if (!Files.exists(path)) return List.of();
        List<Competitor> found = new ArrayList<>();
        // Minimal CSV: reject quoted commas instead of silently misparsing them.
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8).stream().skip(1).toList()) {
            if (line.isBlank() || line.contains("\"")) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 5) continue;
            if (parts[0].trim().equalsIgnoreCase(country) &&
                    sector.toLowerCase().contains(parts[1].trim().toLowerCase()) &&
                    !parts[1].isBlank() && parts[4].trim().startsWith("http")) {
                found.add(new Competitor(parts[2].trim(), parts[3].trim(), parts[4].trim()));
            }
        }
        return found;
    }
}
