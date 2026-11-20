package dataAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Builds parameterized Cosmos DB (NoSQL) queries for products.
 *
 * <p>A query runs in exactly one of two modes:
 * <ul>
 *   <li>{@link #first(int)}: the first {@code count} products the database returns, in no
 *       particular order.</li>
 *   <li>{@link #nearest(int, float[])}: the top {@code count} products ordered by embedding
 *       similarity (most similar first) using {@code VectorDistance}.</li>
 * </ul>
 * Any number of hard filters can be added; they are combined with AND and applied in both modes.
 *
 * <p>Example:
 * <pre>{@code
 * ProductQuery query = new ProductQueryBuilder()
 *         .nearest(20, embedding)
 *         .whereEquals("category", "accessories")
 *         .whereRange("price", 0, 50)
 *         .build();
 * }</pre>
 */
public class ProductQueryBuilder {
    private static final Pattern FIELD_PATH =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
    private static final String ALIAS = "c";

    private enum Mode { FIRST, NEAREST }

    private Mode mode;
    private int count;
    private float[] embedding;
    private String embeddingField = "embedding";

    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private int nextParameterId;

    /**
     * Queries the first {@code count} products the database returns, in no particular order.
     * @param count the maximum number of products to return; must be positive
     * @return this builder
     */
    public ProductQueryBuilder first(int count) {
        setMode(Mode.FIRST, count);
        this.embedding = null;
        return this;
    }

    /**
     * Queries the top {@code count} products, ordered by similarity to the given embedding.
     * @param count the maximum number of products to return; must be positive
     * @param embedding the query embedding; must be non-empty
     * @return this builder
     */
    public ProductQueryBuilder nearest(int count, float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding must be non-empty");
        }
        setMode(Mode.NEAREST, count);
        this.embedding = embedding.clone();
        return this;
    }

    /**
     * Sets the document field holding the vector embedding (default {@code "embedding"}).
     * @param field the field path, e.g. {@code "embedding"} or {@code "vectors.image"}
     * @return this builder
     */
    public ProductQueryBuilder embeddingField(String field) {
        this.embeddingField = validateField(field);
        return this;
    }

    /**
     * Keeps only products whose field equals the value.
     * @param field the field path
     * @param value the required value
     * @return this builder
     */
    public ProductQueryBuilder whereEquals(String field, Object value) {
        return addComparison(field, "=", value);
    }

    /**
     * Keeps only products whose field does not equal the value.
     * @param field the field path
     * @param value the excluded value
     * @return this builder
     */
    public ProductQueryBuilder whereNotEquals(String field, Object value) {
        return addComparison(field, "!=", value);
    }

    /**
     * Keeps only products whose field equals one of the values.
     * @param field the field path
     * @param values the allowed values; must be non-empty
     * @return this builder
     */
    public ProductQueryBuilder whereIn(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values must be non-empty");
        }
        String path = path(field);
        List<String> names = new ArrayList<>();
        for (Object value : values) {
            names.add(addParameter(value));
        }
        conditions.add(path + " IN (" + String.join(", ", names) + ")");
        return this;
    }

    /**
     * Keeps only products whose numeric field is within [min, max]. Either bound may be null
     * to leave that side open.
     * @param field the field path
     * @param min the inclusive lower bound, or null
     * @param max the inclusive upper bound, or null
     * @return this builder
     */
    public ProductQueryBuilder whereRange(String field, Number min, Number max) {
        if (min == null && max == null) {
            throw new IllegalArgumentException("At least one bound must be given");
        }
        if (min != null && max != null && min.doubleValue() > max.doubleValue()) {
            throw new IllegalArgumentException("min must not be greater than max");
        }
        if (min != null) {
            addComparison(field, ">=", min);
        }
        if (max != null) {
            addComparison(field, "<=", max);
        }
        return this;
    }

    /**
     * Keeps only products whose array field contains the value (e.g. a tag or size).
     * @param field the field path of an array
     * @param value the value the array must contain
     * @return this builder
     */
    public ProductQueryBuilder whereArrayContains(String field, Object value) {
        conditions.add("ARRAY_CONTAINS(" + path(field) + ", " + addParameter(value) + ")");
        return this;
    }

    /**
     * Keeps only products whose string field contains the text, ignoring case.
     * @param field the field path of a string
     * @param text the text to look for
     * @return this builder
     */
    public ProductQueryBuilder whereContainsText(String field, String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        conditions.add("CONTAINS(" + path(field) + ", " + addParameter(text) + ", true)");
        return this;
    }

    /**
     * Builds the query.
     * @return the parameterized query
     * @throws IllegalStateException if neither first nor nearest was chosen
     */
    public ProductQuery build() {
        if (mode == null) {
            throw new IllegalStateException("Call first(...) or nearest(...) before build()");
        }
        Map<String, Object> allParameters = new LinkedHashMap<>(parameters);
        StringBuilder query = new StringBuilder("SELECT TOP ")
                .append(count)
                .append(" * FROM ")
                .append(ALIAS);
        if (!conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (mode == Mode.NEAREST) {
            allParameters.put("@embedding", embedding.clone());
            query.append(" ORDER BY VectorDistance(").append(path(embeddingField)).append(", @embedding)");
        }

        return new ProductQuery(query.toString(), allParameters);
    }

    private void setMode(Mode newMode, int newCount) {
        if (newCount <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        this.mode = newMode;
        this.count = newCount;
    }

    private ProductQueryBuilder addComparison(String field, String operator, Object value) {
        conditions.add(path(field) + " " + operator + " " + addParameter(value));
        return this;
    }

    private String addParameter(Object value) {
        String name = "@p" + nextParameterId++;
        parameters.put(name, value);
        return name;
    }

    // Field names are inlined into the query text, so only plain identifiers are accepted.
    private static String validateField(String field) {
        if (field == null || !FIELD_PATH.matcher(field).matches()) {
            throw new IllegalArgumentException("Invalid field path: " + field);
        }
        return field;
    }

    private static String path(String field) {
        return ALIAS + "." + validateField(field);
    }
}
