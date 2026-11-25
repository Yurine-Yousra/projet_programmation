package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SelectTest {

  private Table users;

  @BeforeEach
  void setUp() {
    users = new Table(List.of("id", "name", "age"));
    users.addRow(new Row(List.of(1, "Alice", 22)));
    users.addRow(new Row(List.of(2, "Bob", 30)));
    users.addRow(new Row(List.of(3, "Charlie", 27)));
    users.addRow(new Row(List.of(4, "David", 19)));
  }

  @Test
  void selectAvecInferieurGardeLesBonnesLignes() {
    Operator plan = new Select(new Scan(users), PredicateParser.parse("age < 25"));
    Table result = plan.execute();

    assertEquals(2, result.getRows().size());
    assertEquals("Alice", result.getRows().get(0).getString(1));
    assertEquals("David", result.getRows().get(1).getString(1));
  }

  @Test
  void selectAvecEgaliteSurChaineGardeLaBonneLigne() {
    Operator plan = new Select(new Scan(users), PredicateParser.parse("name = 'Bob'"));
    Table result = plan.execute();

    assertEquals(1, result.getRows().size());
    assertEquals(2, result.getRows().get(0).getInt(0));
    assertEquals("Bob", result.getRows().get(0).getString(1));
  }

  @Test
  void selectAvecSuperieurGardeLesBonnesLignes() {
    Table result = new Select(new Scan(users), PredicateParser.parse("age > 25")).execute();

    assertEquals(List.of(List.of(2, "Bob", 30), List.of(3, "Charlie", 27)),
        result.getRows().stream().map(Row::getValues).toList());
  }

  @Test
  void selectSansResultatGardeLeSchema() {
    Table result = new Select(new Scan(users), PredicateParser.parse("age > 99")).execute();

    assertTrue(result.getRows().isEmpty());
    assertEquals(users.getColumns(), result.getColumns());
  }

  @Test
  void selectSurColonneInconnueEstSignale() {
    Operator plan = new Select(new Scan(users), PredicateParser.parse("taille > 1"));

    assertThrows(IllegalArgumentException.class, plan::execute);
  }
}
