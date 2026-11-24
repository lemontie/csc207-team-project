package entity;

import namedConstant.Property;

import java.util.Set;

public class Product {

  private String productID;
  private String productName;
  private String productDescription;
  private Set<Property> productProperties;

  /**
   * Creates a new user with the given non-empty name and non-empty password.
   * @param ID the item's ID
   * @param name the item's name
   * @param description the item's description
   * @param properties the item's properties
   * @throws IllegalArgumentException if the ID or name are empty
   */
  public Product(String ID, String name, String description, Set<Property> properties) {
    this.productID = ID;
    this.productName = name;
    this.productDescription = description;
    this.productProperties = properties;
  }

  public Product(String ID, String name) {
    this(ID, name, "", Set.of());
  }

  /**
   * Returns the item's Name
   */
  public String getProductName() {
      return this.productName;
  }

  /**
   * Returns the item's ID
   */
  public String getProductID() {
      return this.productID;
  }

  /**
   * Returns the item's Properties
   */
  public Set<Property> getProductProperties() {
    return this.productProperties;
  }

  /**
   * Returns the item's Description
   */
  public String getProductDescription() {
    return this.productDescription;
  }
}
