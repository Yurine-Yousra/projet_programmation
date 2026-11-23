package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnionTest {

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
  void unionContientLesLignesDesDeuxTablesSansDoublon() {
    Table result = new Union(new Scan(gauche), new Scan(droite)).execute();

    assertEquals(List.of("id", "name"), result.getColumns());
    assertEquals(List.of(
        new Row(List.of(1, "Alice")),
        new Row(List.of(2, "Bob")),
        new Row(List.of(3, "Charlie"))), result.getRows());
  }

  @Test
  void unionAvecTableVideRendLaTableSansDoublon() {
    Table vide = new Table(List.of("id", "name"));
    Table result = new Union(new Scan(gauche), new Scan(vide)).execute();

    assertEquals(2, result.getRows().size());
  }

  @Test
  void unionRefuseDesSchemasDifferents() {
    Table autre = new Table(List.of("id", "age"));
    Union union = new Union(new Scan(gauche), new Scan(autre));

    assertThrows(SchemaMismatchException.class, union::execute);
  }

  @Test
  void unionRefuseDesColonnesDansUnAutreOrdre() {
    Table inverse = new Table(List.of("name", "id"));
    Union union = new Union(new Scan(gauche), new Scan(inverse));

    assertThrows(SchemaMismatchException.class, union::execute);
  }
}
