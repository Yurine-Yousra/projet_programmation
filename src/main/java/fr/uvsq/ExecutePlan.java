package fr.uvsq;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/** Point d'entrée : exécute une requête JSON ou traduit une requête .alg. */
public final class ExecutePlan {

  private ExecutePlan() {
  }

  /** Lance l'exécution. */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java -jar td4_alg.jar requete.json | requete.alg");
      return;
    }
    try {
      if (args[0].endsWith(AlgTranslator.EXTENSION)) {
        System.out.println("Traduction en " + AlgTranslator.translateFile(args[0]));
      } else {
        System.out.println("Résultat écrit dans " + run(args[0]));
      }
    } catch (NoSuchFileException e) {
      System.err.println("Erreur : fichier introuvable " + e.getMessage());
      System.exit(1);
    } catch (IOException | RuntimeException e) {
      System.err.println("Erreur : " + e.getMessage());
      System.exit(1);
    }
  }

  /** Charge les tables, construit le plan, l'exécute et écrit le résultat. */
  public static String run(String configFile) throws IOException {
    JsonObject config = JsonParser.parseString(Files.readString(Path.of(configFile)))
        .getAsJsonObject();
    Table result = evaluate(config);
    String output = config.has("output")
        ? config.get("output").getAsString() : "results/output.csv";
    CsvUtils.writeCsv(output, result);
    return output;
  }

  static Table evaluate(JsonObject config) {
    JsonObject tablesJson = PlanBuilder.required(config, "tables").getAsJsonObject();
    Map<String, Table> tables = tablesJson.keySet().stream()
        .collect(Collectors.toMap(name -> name,
            name -> load(tablesJson.get(name).getAsString())));

    JsonObject plan = PlanBuilder.required(config, "plan").getAsJsonObject();
    return new PlanBuilder(tables).build(plan).execute();
  }

  private static Table load(String csvPath) {
    try {
      return CsvUtils.readCsv(csvPath);
    } catch (IOException e) {
      throw new UncheckedIOException("Lecture impossible de " + csvPath, e);
    }
  }
}
