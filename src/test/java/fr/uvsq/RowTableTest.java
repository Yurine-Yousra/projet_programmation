package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class RowTableTest {

  @Test
  void deuxLignesAvecLesMemesValeursSontEgales() {
    Row a = new Row(List.of(1, "Alice"));
    Row b = new Row(List.of(1, "Alice"));

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, new Row(List.of("Alice", 1)));
  }

  @Test
  void accesseursTypes() {
    Row row = new Row(List.of(3, "Bob", 1.5));

    assertEquals(3, row.getInt(0));
    assertEquals("Bob", row.getString(1));
    assertEquals(1.5f, row.getFloat(2));
    assertEquals("[3, Bob, 1.5]", row.toString());
    assertThrows(ClassCastException.class, () -> row.getFloat(1));
  }

  @Test
  void colonneInconnueEstRefusee() {
    Table t = new Table(List.of("id"));

    assertThrows(IllegalArgumentException.class, () -> t.getColumnIndex("name"));
  }

  @Test
  void lignesNonModifiablesDepuisExterieur() {
    Table t = new Table(List.of("id"));
    t.addRow(new Row(List.of(1)));

    assertThrows(UnsupportedOperationException.class,
        () -> t.getRows().add(new Row(List.of(2))));
  }
}
