package fr.uvsq;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Point d'entrée : exécute la requête JSON passée en argument.
 */
public final class ExecutePlan {

  private ExecutePlan() {
  }

  /**
   * Lance l'exécution.
   *
   * @param args le fichier JSON de la requête
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java -jar td4_alg.jar requete.json");
      return;
    }
    try {
      System.out.println("Résultat écrit dans " + run(args[0]));
    } catch (IOException | RuntimeException e) {
      System.err.println("Erreur : " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Charge les tables, construit le plan, l'exécute et écrit le résultat.
   *
   * @param configFile chemin du fichier JSON
   * @return chemin du fichier CSV produit
   * @throws IOException en cas d'erreur de lecture ou d'écriture
   */
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
    JsonObject tablesJson = config.getAsJsonObject("tables");
    Map<String, Table> tables = tablesJson.keySet().stream()
        .collect(Collectors.toMap(name -> name,
            name -> load(tablesJson.get(name).getAsString())));

    return new PlanBuilder(tables).build(config.getAsJsonObject("plan")).execute();
  }

  private static Table load(String csvPath) {
    try {
      return CsvUtils.readCsv(csvPath);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
