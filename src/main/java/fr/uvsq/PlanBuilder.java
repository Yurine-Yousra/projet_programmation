package fr.uvsq;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    register("SCAN", (node, b) -> new Scan(b.table(text(node, "table"))));
    register("SELECT", (node, b) -> new Select(b.input(node, "input"),
        PredicateParser.parse(text(node, "predicate"))));
    register("PROJECT", (node, b) -> new Project(b.input(node, "input"),
        strings(node, "columns")));
    register("JOIN", (node, b) -> new Join(b.input(node, "left"), b.input(node, "right"),
        text(node, "onLeft"), text(node, "onRight")));
    register("UNION", (node, b) -> new Union(b.input(node, "left"), b.input(node, "right")));
    register("INTERSECTION", (node, b) -> new Intersection(b.input(node, "left"),
        b.input(node, "right")));
    register("DIFFERENCE", (node, b) -> new Difference(b.input(node, "left"),
        b.input(node, "right")));
    register("AGGREGATE", PlanBuilder::aggregate);
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
    String type = text(node, "type");
    OperatorFactory factory = factories.get(type);
    if (factory == null) {
      throw new IllegalArgumentException("Type inconnu : " + type);
    }
    return factory.create(node, this);
  }

  Operator input(JsonObject node, String field) {
    return build(required(node, field).getAsJsonObject());
  }

  Table table(String name) {
    Table t = tables.get(name);
    if (t == null) {
      throw new IllegalArgumentException("Table inconnue : " + name);
    }
    return t;
  }

  private static Operator aggregate(JsonObject node, PlanBuilder b) {
    AggregateFunction function = AggregateFunction.of(text(node, "function"));
    String column = node.has("column") ? text(node, "column") : null;
    List<String> groupBy = node.has("groupBy") ? strings(node, "groupBy") : List.of();
    String alias = node.has("as") ? text(node, "as")
        : function.name().toLowerCase(Locale.ROOT) + (column == null ? "" : "_" + column);
    return new Aggregate(b.input(node, "input"), groupBy, function, column, alias);
  }

  static JsonElement required(JsonObject node, String field) {
    JsonElement e = node.get(field);
    if (e == null) {
      throw new IllegalArgumentException("Champ \"" + field + "\" manquant dans " + node);
    }
    return e;
  }

  private static String text(JsonObject node, String field) {
    return required(node, field).getAsString();
  }

  private static List<String> strings(JsonObject node, String field) {
    return required(node, field).getAsJsonArray().asList().stream()
        .map(JsonElement::getAsString)
        .toList();
  }
}
