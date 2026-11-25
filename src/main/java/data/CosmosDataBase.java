package data;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import entity.Product;
import entity.ProductFactory;
import uses.ProductsDataBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Reads products from an Azure Cosmos DB (NoSQL) container, one page at a time.
 * Raw documents are converted to {@link Product}s by {@link ProductFactory}.
 * TODO: use ProductQueryBuilder to build queries using a given InputData object, rather than exposing ProductQuery to the use case.
 */
public class CosmosDataBase implements ProductsDataBase {
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CosmosContainer container;
    private final ProductFactory productFactory;
    private final int pageSize;

    private SqlQuerySpec lastQuery;
    private String nextPageToken;

    /**
     * Connects to the given Cosmos DB container. Don't hardcode the key; read it from an
     * environment variable or an untracked config file.
     * @param endpoint the account endpoint, e.g. {@code https://<account>.documents.azure.com:443/}
     * @param key the account key
     * @param databaseName the database name
     * @param containerName the container holding the products
     */
    public CosmosDataBase(String endpoint, String key, String databaseName, String containerName) {
        this(endpoint, key, databaseName, containerName, DEFAULT_PAGE_SIZE);
    }

    /**
     * Connects to the given Cosmos DB container with a configurable preferred page size.
     * @param endpoint the account endpoint, e.g. {@code https://<account>.documents.azure.com:443/}
     * @param key the account key
     * @param databaseName the database name
     * @param containerName the container holding the products
     * @param pageSize the preferred number of products per page; must be positive
     */
    public CosmosDataBase(
            String endpoint, String key, String databaseName, String containerName, int pageSize) {
        this(new CosmosClientBuilder().endpoint(endpoint).key(key).buildClient()
                        .getDatabase(databaseName).getContainer(containerName),
                pageSize);
    }

    /**
     * Uses an existing container.
     * @param container the container holding the products
     * @param pageSize the preferred number of products per page; must be positive
     */
    public CosmosDataBase(CosmosContainer container, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        this.container = container;
        this.productFactory = new ProductFactory();
        this.pageSize = pageSize;
    }

    /**
     * Starts a new paginated query and returns its first page.
     * @param productQuery the parameterized product query
     * @return the first page, or an empty array if no products match
     */
    @Override
    public Product[] getProducts(ProductQuery productQuery) {
        lastQuery = toQuerySpec(productQuery);
        nextPageToken = null;
        return fetchPage(null);
    }

    /**
     * Returns the next page for the last query supplied to {@link #getProducts(ProductQuery)}.
     * @return the next page, or an empty array after the result set is exhausted
     * @throws IllegalStateException if no query has been started
     */
    @Override
    public Product[] nextProducts() {
        if (lastQuery == null) {
            throw new IllegalStateException("Call getProducts(...) before nextProducts()");
        }
        if (nextPageToken == null) {
            return new Product[0];
        }
        return fetchPage(nextPageToken);
    }

    private Product[] fetchPage(String continuationToken) {
        Iterator<FeedResponse<JsonNode>> pages = container
                .queryItems(lastQuery, new CosmosQueryRequestOptions(), JsonNode.class)
                .iterableByPage(continuationToken, pageSize)
                .iterator();

        // Cross-partition queries can return empty pages that still have more results after them.
        while (pages.hasNext()) {
            FeedResponse<JsonNode> page = pages.next();
            nextPageToken = page.getContinuationToken();
            if (!page.getResults().isEmpty() || nextPageToken == null) {
                return page.getResults().stream().map(productFactory).toArray(Product[]::new);
            }
        }
        nextPageToken = null;
        return new Product[0];
    }

    private static SqlQuerySpec toQuerySpec(ProductQuery productQuery) {
        List<SqlParameter> parameters = new ArrayList<>();
        for (Map.Entry<String, Object> entry : productQuery.getParameters().entrySet()) {
            parameters.add(new SqlParameter(entry.getKey(), entry.getValue()));
        }
        return new SqlQuerySpec(productQuery.getQueryText(), parameters);
    }
}
