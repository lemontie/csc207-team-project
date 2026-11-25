package data;

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
 *   <li>{@link #first()}: products the database returns, in no
 *       particular order.</li>
 *   <li>{@link #nearest(int, float[])}: the closest {@code count} products, ordered by
 *       embedding similarity using {@code VectorDistance}.</li>
 * </ul>
 * Any number of hard filters can be added; they are combined with AND and applied in both modes.
 *
 * <p>Example:
 * <pre>{@code
 * ProductQuery query = new ProductQueryBuilder()
 *         .nearest(20, embedding)
 *         .whereEquals("category", "accessories")
 *         .whereArrayElementRange("offers", "price.price", 0, 50)
 *         .build();
 * }</pre>
 */
public class ProductQueryBuilder {
    private static final Pattern FIELD_PATH =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
    private static final String ALIAS = "c";

    private enum Mode { FIRST, NEAREST }
    private Mode mode;
    private int nearestCount;
    private float[] embedding;
    private String embeddingField = "embedding";

    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private int nextParameterId;

    /**
     * Queries products in no particular order. Use database continuation paging to retrieve
     * the complete result set.
     * @return this builder
     */
    public ProductQueryBuilder first() {
        this.mode = Mode.FIRST;
        this.embedding = null;
        return this;
    }

    /**
     * Queries the closest {@code count} products to the given embedding. Cosmos vector searches
     * require a result limit; continuation paging retrieves up to this limit.
     * @param count the maximum number of products to return; must be positive
     * @param embedding the query embedding; must be non-empty
     * @return this builder
     */
    public ProductQueryBuilder nearest(int count, float[] embedding) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding must be non-empty");
        }
        this.mode = Mode.NEAREST;
        this.nearestCount = count;
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
     * Keeps products with at least one array element whose numeric field is within
     * [min, max]. Either bound may be null to leave that side open. For example,
     * {@code whereArrayElementRange("offers", "price.price", 20, 50)} matches
     * products having any offer priced between 20 and 50, inclusive.
     *
     * @param arrayField the product field containing an array, e.g. {@code "offers"}
     * @param elementField the numeric field path within each element, e.g. {@code "price.price"}
     * @param min the inclusive lower bound, or null
     * @param max the inclusive upper bound, or null
     * @return this builder
     * @throws IllegalArgumentException if both bounds are null or min is greater than max
     */
    public ProductQueryBuilder whereArrayElementRange(
            String arrayField, String elementField, Number min, Number max) {
        if (min == null && max == null) {
            throw new IllegalArgumentException("At least one bound must be given");
        }
        if (min != null && max != null && min.doubleValue() > max.doubleValue()) {
            throw new IllegalArgumentException("min must not be greater than max");
        }

        String arrayPath = path(arrayField);
        String elementPath = "o." + validateField(elementField);
        List<String> elementConditions = new ArrayList<>();
        if (min != null) {
            elementConditions.add(elementPath + " >= " + addParameter(min));
        }
        if (max != null) {
            elementConditions.add(elementPath + " <= " + addParameter(max));
        }
        conditions.add("EXISTS (SELECT VALUE o FROM o IN " + arrayPath
                + " WHERE " + String.join(" AND ", elementConditions) + ")");
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
     * Keeps only products whose embedding is strictly between two vector-distance bounds
     * from the supplied embedding.
     * @param field the document field holding the product embedding
     * @param embedding the reference embedding; must be non-empty
     * @param minDistance the exclusive lower distance bound; must be non-negative
     * @param maxDistance the exclusive upper distance bound; must be greater than minDistance
     * @return this builder
     */
    public ProductQueryBuilder whereEmbeddingDistanceRange(
            String field, float[] embedding, double minDistance, double maxDistance) {
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding must be non-empty");
        }
        if (!Double.isFinite(minDistance) || minDistance < 0) {
            throw new IllegalArgumentException("minDistance must be finite and non-negative");
        }
        if (!Double.isFinite(maxDistance) || maxDistance <= minDistance) {
            throw new IllegalArgumentException("maxDistance must be finite and greater than minDistance");
        }

        String distance = "VectorDistance(" + path(field) + ", " + addParameter(embedding.clone()) + ")";
        conditions.add(distance + " > " + addParameter(minDistance));
        conditions.add(distance + " < " + addParameter(maxDistance));
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
        StringBuilder query = new StringBuilder("SELECT ");
        if (mode == Mode.NEAREST) {
            query.append("TOP ").append(nearestCount).append(' ');
        }
        query.append("* FROM ").append(ALIAS);
        if (!conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (mode == Mode.NEAREST) {
            allParameters.put("@embedding", embedding.clone());
            query.append(" ORDER BY VectorDistance(").append(path(embeddingField)).append(", @embedding)");
        }

        return new ProductQuery(query.toString(), allParameters);
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
