package fr.uvsq;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit un plan d'exécution à partir de sa description JSON.
 *
 * <p>Chaque type d'opérateur est associé à une fabrique dans un registre.
 * Ajouter un opérateur revient à appeler {@link #register}.
 */
public class PlanBuilder {
  private final Map<String, Table> tables;
  private final Map<String, OperatorFactory> factories = new HashMap<>();

  /**
   * Crée un constructeur de plan avec les opérateurs de base.
   *
   * @param tables tables disponibles, par nom
   */
  public PlanBuilder(Map<String, Table> tables) {
    this.tables = Map.copyOf(tables);
    register("SCAN", (node, b) -> new Scan(b.table(node.get("table").getAsString())));
    register("SELECT", (node, b) -> new Select(b.input(node, "input"),
        PredicateParser.parse(node.get("predicate").getAsString())));
    register("PROJECT", (node, b) -> new Project(b.input(node, "input"),
        strings(node, "columns")));
    register("JOIN", (node, b) -> new Join(b.input(node, "left"), b.input(node, "right"),
        node.get("onLeft").getAsString(), node.get("onRight").getAsString()));
    register("UNION", (node, b) -> new Union(b.input(node, "left"), b.input(node, "right")));
  }

  /** Associe un type d'opérateur à sa fabrique. */
  public final void register(String type, OperatorFactory factory) {
    factories.put(type, factory);
  }

  /**
   * Construit l'opérateur décrit par le noeud et ses sous-plans.
   *
   * @param node noeud JSON
   * @return la racine du plan
   */
  public Operator build(JsonObject node) {
    String type = node.get("type").getAsString();
    OperatorFactory factory = factories.get(type);
    if (factory == null) {
      throw new IllegalArgumentException("Type inconnu : " + type);
    }
    return factory.create(node, this);
  }

  Operator input(JsonObject node, String field) {
    return build(node.getAsJsonObject(field));
  }

  Table table(String name) {
    Table t = tables.get(name);
    if (t == null) {
      throw new IllegalArgumentException("Table inconnue : " + name);
    }
    return t;
  }

  static List<String> strings(JsonObject node, String field) {
    return node.getAsJsonArray(field).asList().stream()
        .map(JsonElement::getAsString)
        .toList();
  }
}
