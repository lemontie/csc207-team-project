package uses;

import data.ProductQuery;
import data.ProductQueryBuilder;
import entity.Product;
/**
 * ProductsDataBase is an interface that defines the methods that the ProductsDataBase class must implement.
 */
public interface ProductsDataBase {
    /**
     * Runs the query and returns the first page of matching products.
     * NOTE: THIS METHOD SHOULD NOT RELY ON ProductQuery AND RATHER InputData.
     * #TODO: create InputData and use it here instead of ProductQuery.
     * @param productQuery the query to run, built with {@link ProductQueryBuilder}
     * @return the first page of products, possibly empty
     */
    Product[] getProducts(ProductQuery productQuery);

    /**
     * Returns the next page of products for the most recent query.
     * @return the next page of products, or an empty array if there are no more
     */
    Product[] nextProducts();
}
