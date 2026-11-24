package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PredicateParserTest {

  private final Table users = new Table(List.of("id", "name", "age"));
  private final Row bob = new Row(List.of(2, "Bob", 30));

  private boolean test(String expr) {
    return PredicateParser.parse(expr).test(bob, users);
  }

  @Test
  void comparaisonsNumeriques() {
    assertTrue(test("age > 25"));
    assertFalse(test("age < 25"));
    assertTrue(test("age = 30"));
    assertTrue(test("age >= 30"));
    assertTrue(test("age <= 30"));
    assertTrue(test("age != 31"));
  }

  @Test
  void egaliteSurChaineAvecGuillemets() {
    assertTrue(test("name = 'Bob'"));
    assertFalse(test("name = 'Alice'"));
    assertTrue(test("name != 'Alice'"));
  }

  @Test
  void chaineAvecEspacesEntreGuillemets() {
    Row row = new Row(List.of(5, "Jean Paul", 40));
    assertTrue(PredicateParser.parse("name = 'Jean Paul'").test(row, users));
  }

  @Test
  void predicatIncompletEstRefuse() {
    assertThrows(IllegalArgumentException.class, () -> PredicateParser.parse("age >"));
  }

  @Test
  void inferieurSurChaineEstRefuse() {
    assertThrows(IllegalArgumentException.class, () -> test("name < 'Bob'"));
  }

  @Test
  void colonneInconnueEstRefusee() {
    assertThrows(IllegalArgumentException.class, () -> test("taille > 3"));
  }
}
