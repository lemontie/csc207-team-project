import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import namedConstant.Property;
import org.junit.jupiter.api.Test;
import entity.Item;

import java.util.Set;

class ItemTest {

  @Test
  void normalDescription() {
    assertEquals("Cool", new Item("1", "abc", "Cool", Set.of()).getItemDescription());
  }

  @Test
  void blankDescription() {
    assertTrue(new Item("1", "abc").getItemDescription().isBlank());
  }
}
