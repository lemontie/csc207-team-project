import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import entity.Product;

import java.util.Set;

class ProductTest {

  @Test
  void normalDescription() {
    assertEquals("Cool", new Product("1", "abc", "Cool", Set.of()).getProductDescription());
  }

  @Test
  void blankDescription() {
    assertTrue(new Product("1", "abc").getProductDescription().isBlank());
  }
}
