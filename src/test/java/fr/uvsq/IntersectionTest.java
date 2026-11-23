package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntersectionTest {

  private Table gauche;
  private Table droite;

  @BeforeEach
  void setUp() {
    gauche = new Table(List.of("id", "name"));
    gauche.addRow(new Row(List.of(1, "Alice")));
    gauche.addRow(new Row(List.of(2, "Bob")));
    gauche.addRow(new Row(List.of(2, "Bob")));

    droite = new Table(List.of("id", "name"));
    droite.addRow(new Row(List.of(2, "Bob")));
    droite.addRow(new Row(List.of(3, "Charlie")));
  }

  @Test
  void intersectionGardeLesLignesCommunesUneSeuleFois() {
    Table result = new Intersection(new Scan(gauche), new Scan(droite)).execute();

    assertEquals(List.of("id", "name"), result.getColumns());
    assertEquals(List.of(new Row(List.of(2, "Bob"))), result.getRows());
  }

  @Test
  void intersectionSansLigneCommuneEstVide() {
    Table autre = new Table(List.of("id", "name"));
    autre.addRow(new Row(List.of(4, "David")));

    Table result = new Intersection(new Scan(gauche), new Scan(autre)).execute();

    assertTrue(result.getRows().isEmpty());
  }

  @Test
  void intersectionRefuseDesSchemasDifferents() {
    Table autre = new Table(List.of("id"));
    Intersection op = new Intersection(new Scan(gauche), new Scan(autre));

    assertThrows(SchemaMismatchException.class, op::execute);
  }
}
