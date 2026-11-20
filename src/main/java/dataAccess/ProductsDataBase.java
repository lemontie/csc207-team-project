package dataAccess;

import entity.Product;
/**
 * ProductsDataBase is an interface that defines the methods that the ProductsDataBase class must implement.
 */
public interface ProductsDataBase {
    /**
     * Runs the query and returns the first page of matching products.
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
