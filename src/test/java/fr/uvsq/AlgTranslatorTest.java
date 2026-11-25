package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlgTranslatorTest {

  private static final String TABLES = """
      "tables": {"users": "data/users.csv", "orders": "data/orders.csv"}""";

  private static JsonObject translate(String name) throws IOException {
    return AlgTranslator.translate(Files.readAllLines(Path.of("alg", name + ".alg")));
  }

  private static JsonObject withUsers(String... lines) {
    return AlgTranslator.translate(
        Stream.concat(Stream.of("users: data/users.csv"), Arrays.stream(lines)).toList());
  }

  private static List<Object> names(JsonObject query) {
    Table result = ExecutePlan.evaluate(query);
    int idx = result.getColumnIndex("name");
    return result.getRows().stream().map(row -> row.get(idx)).toList();
  }

  @Test
  void traduitSelectionEtProjection() throws IOException {
    JsonObject expected = JsonParser.parseString("""
        {%s,
         "plan": {"type": "PROJECT", "columns": ["name"],
           "input": {"type": "SELECT", "predicate": "age < 25",
             "input": {"type": "SCAN", "table": "users"}}}}""".formatted(TABLES))
        .getAsJsonObject();

    assertEquals(expected, translate("reqProjSel"));
  }

  @Test
  void traduitLaJointure() throws IOException {
    JsonObject expected = JsonParser.parseString("""
        {%s,
         "plan": {"type": "PROJECT", "columns": ["name", "amount"],
           "input": {"type": "JOIN",
             "left": {"type": "PROJECT", "columns": ["id", "name"],
               "input": {"type": "SCAN", "table": "users"}},
             "right": {"type": "SCAN", "table": "orders"},
             "onLeft": "id", "onRight": "user_id"}}}""".formatted(TABLES))
        .getAsJsonObject();

    assertEquals(expected, translate("optJoin"));
  }

  @Test
  void requeteTraduiteEstExecutable() throws IOException {
    assertEquals(List.of("Alice", "David"), names(translate("reqProjSel")));
    assertEquals(List.of("Alice", "Bob", "Bob", "Charlie"), names(translate("optJoin")));
  }

  @Test
  void chaineAvecEspacesEnDernierePosition() {
    JsonObject query = withUsers("R = SELECT users name = 'Jean Paul'");

    assertEquals("name = 'Jean Paul'",
        query.getAsJsonObject("plan").get("predicate").getAsString());
  }

  @Test
  void traduitLesOperateursEnsemblistes() {
    String jeunes = "A = SELECT users age < 28";
    String vieux = "B = SELECT users age > 20";

    assertEquals(List.of("Alice", "Charlie", "David", "Bob"),
        names(withUsers(jeunes, vieux, "R = UNION A B")));
    assertEquals(List.of("Alice", "Charlie"), names(withUsers(jeunes, vieux, "R = INTER A B")));
    assertEquals(List.of("David"), names(withUsers(jeunes, vieux, "R = DIFF A B")));
  }

  @Test
  void ecritLeFichierJsonSansExecuter(@TempDir Path dir) throws IOException {
    Path alg = dir.resolve("req.alg");
    Files.copy(Path.of("alg/reqProjSel.alg"), alg);

    String json = AlgTranslator.translateFile(alg.toString());

    assertEquals(dir.resolve("req.json").toString(), json);
    String content = Files.readString(Path.of(json));
    assertEquals(translate("reqProjSel"), JsonParser.parseString(content));
    assertFalse(content.contains("\\u003c"));
    assertFalse(Files.exists(dir.resolve("results")));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "R = TRI users age",
      "R = PROJ inconnue name",
      "R = SELECT users age <",
      "R = PROJ users",
      "R = JOIN users users id",
      "R = UNION users",
      "ligne sans sens"})
  void ligneInvalideEstSignalee(String line) {
    assertThrows(IllegalArgumentException.class, () -> withUsers(line));
  }

  @Test
  void fichierSansRequeteEstSignale() {
    assertThrows(IllegalArgumentException.class, () -> withUsers());
  }
}
