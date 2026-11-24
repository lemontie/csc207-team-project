package entity;

import namedConstant.Property;

import java.util.Set;

public class Item {

  private String itemID;
  private String itemName;
  private String itemDescription;
  private Set<Property> itemProperties;

  /**
   * Creates a new user with the given non-empty name and non-empty password.
   * @param ID the item's ID
   * @param name the item's name
   * @param description the item's description
   * @param properties the item's properties
   * @throws IllegalArgumentException if the ID or name are empty
   */
  public Item(String ID, String name, String description, Set<Property> properties) {
    this.itemID = ID;
    this.itemName = name;
    this.itemDescription = description;
    this.itemProperties = properties;
  }

  public Item(String ID, String name) {
    this(ID, name, "", Set.of());
  }

  /**
   * Returns the item's Name
   */
  public String getItemName() {
      return this.itemName;
  }

  /**
   * Returns the item's ID
   */
  public String getItemID() {
      return this.itemID;
  }

  /**
   * Returns the item's Properties
   */
  public Set<Property> getItemProperties() {
    return this.itemProperties;
  }

  /**
   * Returns the item's Description
   */
  public String getItemDescription() {
    return this.itemDescription;
  }
}
