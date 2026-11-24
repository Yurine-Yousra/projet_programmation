package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlanBuilderTest {

  private static final String SCAN = """
      {"type": "SCAN", "table": "users"}""";

  private PlanBuilder builder;

  @BeforeEach
  void setUp() {
    Table users = new Table(List.of("id", "name"));
    users.addRow(new Row(List.of(1, "Alice")));
    builder = new PlanBuilder(Map.of("users", users));
  }

  private Operator build(String json) {
    return builder.build(JsonParser.parseString(json).getAsJsonObject());
  }

  private Operator buildBinary(String type) {
    return build("""
        {"type": "%s", "left": %s, "right": %s}""".formatted(type, SCAN, SCAN));
  }

  @Test
  void construitLesOperateursUnaires() {
    assertInstanceOf(Scan.class, build(SCAN));
    assertInstanceOf(Select.class, build("""
        {"type": "SELECT", "predicate": "id = 1", "input": %s}""".formatted(SCAN)));
    assertInstanceOf(Project.class, build("""
        {"type": "PROJECT", "columns": ["name"], "input": %s}""".formatted(SCAN)));
    assertInstanceOf(Aggregate.class, build("""
        {"type": "AGGREGATE", "function": "count", "input": %s}""".formatted(SCAN)));
  }

  @Test
  void construitLesOperateursBinaires() {
    assertInstanceOf(Join.class, build("""
        {"type": "JOIN", "left": %s, "right": %s, "onLeft": "id", "onRight": "id"}"""
        .formatted(SCAN, SCAN)));
    assertInstanceOf(Union.class, buildBinary("UNION"));
    assertInstanceOf(Intersection.class, buildBinary("INTERSECTION"));
    assertInstanceOf(Difference.class, buildBinary("DIFFERENCE"));
  }

  @Test
  void nomParDefautDeLaColonneAgregee() {
    Table t = build("""
        {"type": "AGGREGATE", "function": "MAX", "column": "id", "input": %s}"""
        .formatted(SCAN)).execute();

    assertEquals(List.of("max_id"), t.getColumns());
  }

  @Test
  void typeInconnuEstRefuse() {
    assertThrows(IllegalArgumentException.class, () -> build("""
        {"type": "TRI"}"""));
  }

  @Test
  void tableInconnueEstRefusee() {
    assertThrows(IllegalArgumentException.class, () -> build("""
        {"type": "SCAN", "table": "clients"}"""));
  }

  @Test
  void nouvelOperateurAjouteSansModifierLaClasse() {
    builder.register("VIDE", (node, b) -> () -> new Table(List.of("x")));

    Table t = build("""
        {"type": "VIDE"}""").execute();

    assertEquals(List.of("x"), t.getColumns());
  }
}
