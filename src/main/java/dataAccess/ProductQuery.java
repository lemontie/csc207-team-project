package dataAccess;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An immutable, parameterized Cosmos DB (NoSQL) query produced by {@link ProductQueryBuilder}.
 * Values are never inlined into the query text; they are passed separately as named parameters
 * (e.g. convert to a {@code SqlQuerySpec} with one {@code SqlParameter} per entry).
 */
public final class ProductQuery {
    private final String queryText;
    private final Map<String, Object> parameters;

    ProductQuery(String queryText, Map<String, Object> parameters) {
        this.queryText = queryText;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
    }

    public String getQueryText() {
        return queryText;
    }

    /**
     * Returns the query parameters, keyed by name (including the leading '@').
     * @return an unmodifiable map of parameter names to values
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return queryText + " " + parameters.keySet();
    }
}
