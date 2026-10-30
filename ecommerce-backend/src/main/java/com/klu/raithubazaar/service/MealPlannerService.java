package com.klu.raithubazaar.service;

import com.fasterxml.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.klu.raithubazaar.dto.MealPlanRequest;
import com.klu.raithubazaar.dto.MealPlanResponse;
import com.klu.raithubazaar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MealPlannerService {
    private final ProductRepository products;
    private final JsonMapper mapper;
    private final RestClient ollamaClient;
    private final RestClient openRouterClient;
    private final String provider;
    private final String ollamaModel;
    private final String openRouterModel;
    private final String openRouterApiKey;

    public MealPlannerService(ProductRepository products, JsonMapper mapper,
                              @Value("${app.ai.ollama-url:http://localhost:11434}") String ollamaUrl,
                              @Value("${app.ai.provider:ollama}") String provider,
                              @Value("${app.ai.ollama-model:qwen3:4b}") String ollamaModel,
                              @Value("${app.ai.openrouter-url:https://openrouter.ai/api/v1}") String openRouterUrl,
                              @Value("${app.ai.openrouter-model:openrouter/free}") String openRouterModel,
                              @Value("${app.ai.openrouter-api-key:}") String openRouterApiKey) {
        this.products = products;
        this.mapper = mapper;
        this.provider = provider;
        this.ollamaModel = ollamaModel;
        this.openRouterModel = openRouterModel;
        this.openRouterApiKey = openRouterApiKey;
        this.ollamaClient = RestClient.builder().baseUrl(ollamaUrl).build();
        this.openRouterClient = RestClient.builder().baseUrl(openRouterUrl).build();
    }

    public MealPlanResponse suggest(MealPlanRequest request) {
        if (request == null || request.dish() == null || request.dish().isBlank() || request.dish().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a dish name up to 120 characters.");
        }
        int servings = request.servings() == null ? 2 : request.servings();
        if (servings < 1 || servings > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servings must be between 1 and 20.");
        }
        String system = "You are the Raithu Bazaar meal-planning assistant. Suggest only vegetables needed for the requested dish. "
                + "Give practical quantities for the total servings. Exclude spices, oils, grains, meat, and cooking instructions. "
                + "Never invent prices, stock, product IDs, or availability. The store matches ingredient names against its live catalog. "
                + "If the dish is unclear or not a meal, return an empty ingredients array and a short helpful note. "
                + "Return a JSON object with note and ingredients; each ingredient has name, quantity, and note.";
        String input = "Dish: " + request.dish().trim() + "\nServings: " + servings;
        try {
            String text = "openrouter".equalsIgnoreCase(provider)
                    ? generateWithOpenRouter(system, input)
                    : generateWithOllama(system, input);
            if (text.isBlank()) throw new IllegalStateException("Empty local model response");
            JsonNode plan = mapper.readTree(text);
            List<MealPlanResponse.Ingredient> ingredients = new ArrayList<>();
            JsonNode rows = plan.path("ingredients");
            if (rows.isArray()) {
                for (JsonNode row : rows) {
                    String name = row.path("name").asString("").trim();
                    if (name.isBlank() || name.length() > 80) continue;
                    String quantity = row.path("quantity").asString("As needed");
                    String note = row.path("note").asString("");
                    List<MealPlanResponse.ProductMatch> matches = findMatches(name);
                    ingredients.add(new MealPlanResponse.Ingredient(name, quantity, note, matches));
                    if (ingredients.size() == 15) break;
                }
            }
            return new MealPlanResponse(request.dish().trim(), servings,
                    plan.path("note").asString("Suggested vegetables for your dish."), ingredients);
        } catch (ResourceAccessException e) {
            String message = "openrouter".equalsIgnoreCase(provider)
                    ? "The hosted AI service is temporarily unavailable. Please try again shortly."
                    : "Start Ollama and download the qwen3:4b model to enable local meal suggestions.";
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            if ("openrouter".equalsIgnoreCase(provider) && e.getStatusCode().value() == 429) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "The hosted free-model request limit has been reached. Please try again later.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The meal suggestion service could not respond. Please try again shortly.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The meal suggestion model could not respond. Please try again shortly.");
        }
    }

    private String generateWithOllama(String system, String prompt) {
        JsonNode response = ollamaClient.post().uri("/api/generate")
                .body(mapper.createObjectNode().put("model", ollamaModel).put("system", system)
                        .put("prompt", prompt).put("format", "json").put("stream", false).put("think", false))
                .retrieve().body(JsonNode.class);
        return response == null ? "" : response.path("response").asString("");
    }

    private String generateWithOpenRouter(String system, String prompt) {
        if (openRouterApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Set OPENROUTER_API_KEY in the backend or deployment secrets to use the hosted model.");
        }
        var messages = mapper.createArrayNode()
                .add(mapper.createObjectNode().put("role", "system").put("content", system))
                .add(mapper.createObjectNode().put("role", "user").put("content", prompt));
        var body = mapper.createObjectNode();
        body.put("model", openRouterModel);
        body.set("messages", messages);
        body.put("max_tokens", 700);
        body.put("temperature", 0.2);
        JsonNode response = openRouterClient.post().uri("/chat/completions")
                .header("Authorization", "Bearer " + openRouterApiKey)
                .header("X-Title", "Raithu Bazaar")
                .body(body)
                .retrieve().body(JsonNode.class);
        JsonNode content = response == null ? null : response.path("choices").path(0).path("message").path("content");
        if (content != null && content.isString()) return content.asString();
        if (content != null && content.isArray()) {
            StringBuilder text = new StringBuilder();
            for (JsonNode part : content) {
                if (part.hasNonNull("text")) text.append(part.path("text").asString());
            }
            return text.toString();
        }
        return "";
    }

    private List<MealPlanResponse.ProductMatch> findMatches(String ingredient) {
        String target = normalize(ingredient);
        Set<String> words = new HashSet<>(Arrays.asList(target.split(" ")));
        return products.findAll().stream()
                .filter(p -> "Vegetables".equalsIgnoreCase(p.getCategory()) && p.getStock() != null && p.getStock() > 0)
                .filter(p -> {
                    String name = normalize(p.getProductName());
                    return name.contains(target) || target.contains(name)
                            || words.stream().filter(w -> w.length() > 2 && name.contains(w)).count() > 0;
                })
                .limit(3)
                .map(p -> new MealPlanResponse.ProductMatch(p.getProductId(), p.getProductName(), p.getUnit(),
                        p.getPrice(), p.getStock(), p.getImageUrl()))
                .toList();
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim()
                .replaceAll("^(fresh|farm|local|green|red|desi|organic)\\s+", "")
                .replaceAll("\\b(tomatoes)\\b", "tomato")
                .replaceAll("\\b(potatoes)\\b", "potato")
                .replaceAll("\\b(\\w+)s\\b", "$1");
    }
}
