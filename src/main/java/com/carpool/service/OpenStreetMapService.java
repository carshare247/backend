package com.carpool.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OpenStreetMapService {
    private final RestClient client = RestClient.builder()
        .baseUrl("https://photon.komoot.io")
        .defaultHeader("User-Agent", "CarShare247/1.0 location-search")
        .build();
    private final Map<String, CachedPlaces> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MILLIS = 30L * 24 * 60 * 60 * 1000;

    public synchronized List<Place> search(String query, String state) {
        String cacheKey = (query.trim().toLowerCase() + "|" + (state == null ? "" : state.trim().toLowerCase()));
        CachedPlaces cached = cache.get(cacheKey);
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) return cached.places;

        List<Place> places = searchPhoton(query, state);
        cache.put(cacheKey, new CachedPlaces(places, System.currentTimeMillis() + CACHE_TTL_MILLIS));
        return places;
    }

    private List<Place> searchPhoton(String query, String state) {
        try {
            JsonNode response = client.get()
                .uri(uri -> uri.path("/api/")
                    .queryParam("q", query + (state == null || state.isBlank() ? ", India" : ", " + state + ", India"))
                    .queryParam("limit", "10")
                    .build())
                .retrieve().body(JsonNode.class);
            List<Place> places = new ArrayList<>();
            if (response != null && response.path("features").isArray()) {
                for (JsonNode feature : response.path("features")) {
                    JsonNode properties = feature.path("properties");
                    if (!"india".equalsIgnoreCase(properties.path("country").asText())) continue;
                    JsonNode coordinates = feature.path("geometry").path("coordinates");
                    if (!coordinates.isArray() || coordinates.size() < 2) continue;
                    String city = firstPhoton(properties, "city", "district", "county", "municipality");
                    String providerState = properties.path("state").asText(null);
                    String displayName = joinPlaceParts(
                        properties.path("name").asText(query), city, providerState, "India");
                    places.add(new Place(
                        properties.path("osm_id").asText(null),
                        displayName,
                        new BigDecimal(coordinates.get(1).asText()),
                        new BigDecimal(coordinates.get(0).asText()),
                        "[]",
                        city,
                        providerState,
                        "India",
                        properties.path("type").asText("unknown").toUpperCase(),
                        1000));
                }
            }
            return places;
        } catch (Exception exception) {
            log.warn("Photon fallback search failed for query {}", query, exception);
            return List.of();
        }
    }

    private Place toPlace(JsonNode item) {
        JsonNode address = item.path("address");
        String type = item.path("type").asText("unknown").toUpperCase();
        return new Place(
            item.path("osm_id").asText(null),
            item.path("display_name").asText(),
            new BigDecimal(item.path("lat").asText("0")),
            new BigDecimal(item.path("lon").asText("0")),
            item.path("boundingbox").toString(),
            first(address, "city", "town", "municipality", "village"),
            first(address, "state"),
            first(address, "country"),
            type,
            radius(type));
    }

    private String firstPhoton(JsonNode node, String... names) {
        for (String name : names) {
            if (node.hasNonNull(name) && !node.get(name).asText().isBlank()) return node.get(name).asText();
        }
        return null;
    }

    private String joinPlaceParts(String... parts) {
        return java.util.Arrays.stream(parts)
            .filter(part -> part != null && !part.isBlank())
            .distinct()
            .reduce((first, second) -> first + ", " + second)
            .orElse("");
    }

    private String first(JsonNode node, String... names) {
        for (String name : names) if (node.hasNonNull(name)) return node.get(name).asText();
        return null;
    }

    private int radius(String type) {
        if (type.contains("street") || type.contains("road")) return 200;
        if (type.contains("city") || type.contains("town")) return 5000;
        if (type.contains("suburb") || type.contains("neighbourhood")) return 1000;
        return 500;
    }

    private record CachedPlaces(List<Place> places, long expiresAt) {}

    public record Place(String osmId, String displayName, BigDecimal latitude, BigDecimal longitude,
                        String boundingBox, String city, String state, String country,
                        String locationType, int geofenceRadius) {}
}