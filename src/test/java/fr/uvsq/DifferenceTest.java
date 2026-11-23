package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DifferenceTest {

  private Table gauche;
  private Table droite;

  @BeforeEach
  void setUp() {
    gauche = new Table(List.of("id", "name"));
    gauche.addRow(new Row(List.of(1, "Alice")));
    gauche.addRow(new Row(List.of(1, "Alice")));
    gauche.addRow(new Row(List.of(2, "Bob")));

    droite = new Table(List.of("id", "name"));
    droite.addRow(new Row(List.of(2, "Bob")));
    droite.addRow(new Row(List.of(3, "Charlie")));
  }

  @Test
  void differenceRetireLesLignesPresentesDansDroite() {
    Table result = new Difference(new Scan(gauche), new Scan(droite)).execute();

    assertEquals(List.of(new Row(List.of(1, "Alice"))), result.getRows());
  }

  @Test
  void differenceEstAsymetrique() {
    Table result = new Difference(new Scan(droite), new Scan(gauche)).execute();

    assertEquals(List.of(new Row(List.of(3, "Charlie"))), result.getRows());
  }

  @Test
  void differenceAvecElleMemeEstVide() {
    Table result = new Difference(new Scan(gauche), new Scan(gauche)).execute();

    assertTrue(result.getRows().isEmpty());
  }

  @Test
  void differenceRefuseDesSchemasDifferents() {
    Table autre = new Table(List.of("name", "id"));
    Difference op = new Difference(new Scan(gauche), new Scan(autre));

    assertThrows(SchemaMismatchException.class, op::execute);
  }
}
