package fr.uvsq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Traduit une requête écrite dans un fichier .alg en requête JSON. */
public final class AlgTranslator {

  /** Extension des fichiers à traduire. */
  public static final String EXTENSION = ".alg";

  private static final Gson GSON =
      new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  private static final Map<String, String> SET_OPERATIONS = Map.of(
      "UNION", "UNION", "INTER", "INTERSECTION", "DIFF", "DIFFERENCE");

  private final JsonObject tables = new JsonObject();
  private final Map<String, JsonObject> results = new HashMap<>();
  private JsonObject plan;

  private AlgTranslator() {
  }

  /** Traduit le fichier .alg et renvoie le nom du fichier JSON écrit. */
  public static String translateFile(String algFile) throws IOException {
    String jsonFile = algFile.substring(0, algFile.length() - EXTENSION.length()) + ".json";
    JsonObject query = translate(Files.readAllLines(Path.of(algFile)));
    Files.writeString(Path.of(jsonFile), GSON.toJson(query) + System.lineSeparator());
    return jsonFile;
  }

  /** Traduit les lignes d'une requête .alg. */
  static JsonObject translate(List<String> lines) {
    AlgTranslator translator = new AlgTranslator();
    lines.stream()
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .forEach(translator::readLine);
    if (translator.plan == null) {
      throw new IllegalArgumentException("Aucune requête dans le fichier");
    }
    JsonObject query = new JsonObject();
    query.add("tables", translator.tables);
    query.add("plan", translator.plan);
    return query;
  }

  private void readLine(String line) {
    String[] words = line.split("\\s+");
    if (words.length >= 3 && words[1].equals("=")) {
      plan = operator(line, words);
      results.put(words[0], plan);
      return;
    }
    String[] table = line.split("\\s*:\\s*", 2);
    if (table.length != 2 || table[0].isEmpty() || table[1].isEmpty()) {
      throw new IllegalArgumentException("Ligne invalide : " + line);
    }
    tables.addProperty(table[0], table[1]);
  }

  private JsonObject operator(String line, String[] words) {
    String name = words[2];
    JsonObject node = new JsonObject();
    if (name.equals("SELECT")) {
      String[] parts = line.split("\\s+", 7);
      check(parts.length == 7, line);
      node.addProperty("type", "SELECT");
      node.addProperty("predicate", parts[4] + " " + parts[5] + " " + parts[6]);
      node.add("input", input(parts[3]));
    } else if (name.equals("PROJ")) {
      check(words.length >= 5, line);
      JsonArray columns = new JsonArray();
      Arrays.stream(words).skip(4).forEach(columns::add);
      node.addProperty("type", "PROJECT");
      node.add("columns", columns);
      node.add("input", input(words[3]));
    } else if (name.equals("JOIN")) {
      check(words.length == 7, line);
      binary(node, "JOIN", words);
      node.addProperty("onLeft", words[5]);
      node.addProperty("onRight", words[6]);
    } else if (SET_OPERATIONS.containsKey(name)) {
      check(words.length == 5, line);
      binary(node, SET_OPERATIONS.get(name), words);
    } else {
      throw new IllegalArgumentException("Opérateur inconnu : " + name);
    }
    return node;
  }

  private void binary(JsonObject node, String type, String[] words) {
    node.addProperty("type", type);
    node.add("left", input(words[3]));
    node.add("right", input(words[4]));
  }

  private JsonObject input(String name) {
    if (results.containsKey(name)) {
      return results.get(name);
    }
    if (!tables.has(name)) {
      throw new IllegalArgumentException("Relation inconnue : " + name);
    }
    JsonObject scan = new JsonObject();
    scan.addProperty("type", "SCAN");
    scan.addProperty("table", name);
    return scan;
  }

  private static void check(boolean valid, String line) {
    if (!valid) {
      throw new IllegalArgumentException("Ligne invalide : " + line);
    }
  }
}
