package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Exécute les plans du dossier plans sur les données du dossier data.
 */
class ExecutePlanTest {

  private static JsonObject read(String plan) throws IOException {
    return JsonParser.parseString(Files.readString(Path.of("plans", plan + ".json")))
        .getAsJsonObject();
  }

  @ParameterizedTest
  @ValueSource(strings = {"plan1", "plan_union", "plan_intersection", "plan_difference",
      "plan_aggregate"})
  void planDonneLeResultatAttendu(String plan) throws IOException {
    Table expected = CsvUtils.readCsv("expected/" + plan + "_expected.csv");

    Table result = ExecutePlan.evaluate(read(plan));

    assertEquals(expected.getColumns(), result.getColumns());
    assertEquals(expected.getRows(), result.getRows());
  }

  @Test
  void schemasIncompatiblesSontSignales() throws IOException {
    JsonObject config = read("plan_schema_incompatible");

    assertThrows(SchemaMismatchException.class, () -> ExecutePlan.evaluate(config));
  }

  @Test
  void runEcritLeFichierDeSortie(@TempDir Path dir) throws IOException {
    Path out = dir.resolve("results/output.csv");
    JsonObject config = read("plan1");
    config.addProperty("output", out.toString());
    Path json = dir.resolve("plan1.json");
    Files.writeString(json, config.toString());

    ExecutePlan.run(json.toString());

    assertEquals(Files.readAllLines(Path.of("expected/plan1_expected.csv")),
        Files.readAllLines(out));
  }

  @ParameterizedTest
  @ValueSource(strings = {"tables", "plan"})
  void champManquantEstSignale(String field) throws IOException {
    JsonObject config = read("plan1");
    config.remove(field);

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> ExecutePlan.evaluate(config));

    assertTrue(e.getMessage().contains(field));
  }

  @Test
  void tableIntrouvableEstSignalee() throws IOException {
    JsonObject config = read("plan1");
    config.getAsJsonObject("tables").addProperty("users", "absent.csv");

    UncheckedIOException e = assertThrows(UncheckedIOException.class,
        () -> ExecutePlan.evaluate(config));

    assertTrue(e.getMessage().contains("absent.csv"));
  }
}
