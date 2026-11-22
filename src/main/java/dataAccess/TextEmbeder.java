package dataAccess;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates text embeddings through an Azure Foundry embeddings endpoint.
 */
public class TextEmbeder {
    private static final String DEFAULT_API_VERSION = "2024-10-21";
    private final URI endpoint;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public TextEmbeder() {
        this(
                requireEnvironment("AZURE_FOUNDRY_ENDPOINT"),
                requireEnvironment("AZURE_FOUNDRY_API_KEY"),
                environmentOrDefault("AZURE_FOUNDRY_EMBEDDING_DEPLOYMENT", null));
    }

    public TextEmbeder(String endpoint, String apiKey, String model) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Embedding endpoint must not be empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Embedding API key must not be empty");
        }
        this.apiKey = apiKey;
        this.model = model == null || model.trim().isEmpty() ? null : model.trim();
        this.endpoint = URI.create(buildEndpoint(endpoint, this.model));
        this.httpClient = HttpClient.newHttpClient();
    }

    public TextEmbeder(String endpoint, String apiKey) {
        this(endpoint, apiKey, null);
    }

    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text must not be empty");
        }

        String payload;
        if (model != null) {
            payload = "{\"input\":" + quote(text)
                    + ",\"model\":" + quote(model) + "}";
        } else {
            payload = "{\"input\":" + quote(text) + "}";
        }

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Azure Foundry embedding request failed with HTTP " + response.statusCode()
                                + ": " + response.body());
            }
            return parseEmbedding(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Azure Foundry embedding request was interrupted", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to call Azure Foundry embedding endpoint", exception);
        }
    }

    public float[] embedText(String text) {
        return embed(text);
    }

    public URI getEndpoint() {
        return endpoint;
    }

    private static float[] parseEmbedding(String json) {
        Matcher arrayMatcher = Pattern.compile("\"embedding\"\\s*:\\s*\\[(.*?)\\]",
                Pattern.DOTALL).matcher(json);
        if (!arrayMatcher.find()) {
            throw new IllegalStateException("Azure Foundry response did not contain an embedding");
        }

        String[] parts = arrayMatcher.group(1).split(",");
        List<Float> values = new ArrayList<>(parts.length);
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                values.add(Float.parseFloat(trimmed));
            }
        }

        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private static String buildEndpoint(String endpoint, String model) {
        String value = stripTrailingSlash(endpoint.trim());
        if (value.endsWith("/embeddings")) {
            return value;
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Embedding model/deployment name is required");
        }
        if (value.endsWith("/openai/v1")) {
            return value + "/embeddings";
        }
        return value + "/openai/deployments/"
                + encodePathSegment(model)
                + "/embeddings?api-version=" + DEFAULT_API_VERSION;
    }

    private static String encodePathSegment(String value) {
        return value.trim().replace(" ", "%20");
    }

    private static String stripTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String quote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static String requireEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return value;
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

}
