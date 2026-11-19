package dataAccess;

import entity.Product;
/**
 * ProductsDataBase is an interface that defines the methods that the ProductsDataBase class must implement.
 * Probably want to add a ProductFactory to this interface to create Product objects from the API response.
 */
public interface ProductsDataBase {
    // TODO: Add parameters to methods
    Product[] getProducts();
}
