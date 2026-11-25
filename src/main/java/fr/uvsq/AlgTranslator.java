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

  /** Traduction d'une ligne "nom = MOT-CLÉ arguments" en noeud JSON. */
  @FunctionalInterface
  private interface Rule {
    JsonObject translate(AlgTranslator translator, String line, String[] words);
  }

  private static final Map<String, Rule> RULES = Map.of(
      "SELECT", AlgTranslator::select,
      "PROJ", AlgTranslator::project,
      "JOIN", AlgTranslator::join,
      "UNION", setOperation("UNION"),
      "INTER", setOperation("INTERSECTION"),
      "DIFF", setOperation("DIFFERENCE"));

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
    Rule rule = RULES.get(words[2]);
    if (rule == null) {
      throw new IllegalArgumentException("Opérateur inconnu : " + words[2]);
    }
    return rule.translate(this, line, words);
  }

  private JsonObject select(String line, String[] words) {
    String[] parts = line.split("\\s+", 7);
    check(parts.length == 7, line);
    JsonObject node = node("SELECT");
    node.addProperty("predicate", parts[4] + " " + parts[5] + " " + parts[6]);
    node.add("input", input(parts[3]));
    return node;
  }

  private JsonObject project(String line, String[] words) {
    check(words.length >= 5, line);
    JsonArray columns = new JsonArray();
    Arrays.stream(words).skip(4).forEach(columns::add);
    JsonObject node = node("PROJECT");
    node.add("columns", columns);
    node.add("input", input(words[3]));
    return node;
  }

  private JsonObject join(String line, String[] words) {
    check(words.length == 7, line);
    JsonObject node = binary("JOIN", words);
    node.addProperty("onLeft", words[5]);
    node.addProperty("onRight", words[6]);
    return node;
  }

  private static Rule setOperation(String type) {
    return (translator, line, words) -> {
      check(words.length == 5, line);
      return translator.binary(type, words);
    };
  }

  private JsonObject binary(String type, String[] words) {
    JsonObject node = node(type);
    node.add("left", input(words[3]));
    node.add("right", input(words[4]));
    return node;
  }

  private static JsonObject node(String type) {
    JsonObject node = new JsonObject();
    node.addProperty("type", type);
    return node;
  }

  private JsonObject input(String name) {
    if (results.containsKey(name)) {
      return results.get(name);
    }
    if (!tables.has(name)) {
      throw new IllegalArgumentException("Relation inconnue : " + name);
    }
    JsonObject scan = node("SCAN");
    scan.addProperty("table", name);
    return scan;
  }

  private static void check(boolean valid, String line) {
    if (!valid) {
      throw new IllegalArgumentException("Ligne invalide : " + line);
    }
  }
}
