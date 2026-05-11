package com.natation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ChatbotService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String answer(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Please type a question about natation, clubs, rankings, competitions, or swimmers.";
        }

        String normalized = question.toLowerCase();
        if (isPlannedFeatureQuestion(normalized)) {
            return "That section is visible in the member portal, but it is not implemented yet.";
        }

        if (isCurrentBestSwimmerQuestion(normalized)) {
            return "There is no single official “best swimmer” for everyone, because awards are split by men and women. "
                    + "The latest official World Aquatics 2025 Swimmers of the Year are Léon Marchand of France and Summer McIntosh of Canada. "
                    + "Source: https://www.worldaquatics.com/news/4417577/world-aquatics-announces-2025-swimmers-of-the-year-marchand-and-mcintosh-reign-again";
        }

        String wikipediaAnswer = lookupWikipedia(question);
        if (wikipediaAnswer != null) {
            return wikipediaAnswer;
        }

        String duckDuckGoAnswer = lookupDuckDuckGo(question);
        if (duckDuckGoAnswer != null) {
            return duckDuckGoAnswer;
        }

        return "I could not find a reliable web answer for that question. Try asking with more specific natation keywords, such as swimmer name, event, year, or competition.";
    }

    private boolean isPlannedFeatureQuestion(String question) {
        return containsAny(question, "competition", "planned", "calendar", "document", "news", "media", "performance dashboard", "results engine");
    }

    private boolean isCurrentBestSwimmerQuestion(String question) {
        return containsAny(question, "best swimmer", "top swimmer", "greatest swimmer", "swimmer in world", "swimmer in the world");
    }

    private String lookupWikipedia(String question) {
        try {
            String searchTerm = buildSearchTerm(question);
            URI searchUri = UriComponentsBuilder
                    .fromUriString("https://en.wikipedia.org/w/api.php")
                    .queryParam("action", "query")
                    .queryParam("list", "search")
                    .queryParam("srsearch", searchTerm)
                    .queryParam("format", "json")
                    .build()
                    .encode()
                    .toUri();

            JsonNode searchJson = getJson(searchUri);
            JsonNode firstResult = searchJson.path("query").path("search").path(0);
            String title = firstResult.path("title").asText("");
            if (title.isBlank()) {
                return null;
            }

            URI summaryUri = UriComponentsBuilder
                    .fromUriString("https://en.wikipedia.org/api/rest_v1/page/summary/{title}")
                    .buildAndExpand(title)
                    .encode()
                    .toUri();

            JsonNode summaryJson = getJson(summaryUri);
            String extract = summaryJson.path("extract").asText("");
            if (extract.isBlank()) {
                return null;
            }

            String pageUrl = summaryJson.path("content_urls").path("desktop").path("page").asText("");
            String source = pageUrl.isBlank() ? "" : " Source: " + pageUrl;
            return summaryJson.path("title").asText(title) + ": " + extract + source;
        } catch (Exception error) {
            System.err.println("Wikipedia lookup failed: " + error.getMessage());
            return null;
        }
    }

    private String lookupDuckDuckGo(String question) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.duckduckgo.com/")
                    .queryParam("q", buildSearchTerm(question))
                    .queryParam("format", "json")
                    .queryParam("no_html", "1")
                    .queryParam("skip_disambig", "1")
                    .build()
                    .encode()
                    .toUri();

            JsonNode json = getJson(uri);
            String abstractText = json.path("AbstractText").asText("");
            if (abstractText.isBlank()) {
                return null;
            }

            String sourceUrl = json.path("AbstractURL").asText("");
            String source = sourceUrl.isBlank() ? "" : " Source: " + sourceUrl;
            return abstractText + source;
        } catch (Exception error) {
            System.err.println("DuckDuckGo lookup failed: " + error.getMessage());
            return null;
        }
    }

    private JsonNode getJson(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "NatationClubChatbot/1.0")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private String buildSearchTerm(String question) {
        String normalized = question.toLowerCase();
        if (containsAny(normalized, "world record")) return question + " swimming world record";
        if (containsAny(normalized, "olympic", "olympics")) return question + " swimming olympics";
        if (containsAny(normalized, "natation", "swim", "swimmer", "aquatics")) return question;
        return question + " swimming natation";
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
