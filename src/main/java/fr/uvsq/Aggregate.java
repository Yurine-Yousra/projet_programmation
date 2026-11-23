package fr.uvsq;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Agrégation avec regroupement facultatif, comme GROUP BY en SQL.
 *
 * <p>Le résultat contient les colonnes de regroupement puis une colonne
 * avec la valeur agrégée. Les groupes gardent l'ordre de leur première
 * apparition.
 */
public class Aggregate implements Operator {
  private final Operator input;
  private final List<String> groupBy;
  private final AggregateFunction function;
  private final String column;
  private final String alias;

  /**
   * Crée une agrégation.
   *
   * @param input sous-plan
   * @param groupBy colonnes de regroupement, éventuellement vide
   * @param function fonction à appliquer
   * @param column colonne agrégée, peut être null pour COUNT
   * @param alias nom de la colonne résultat
   */
  public Aggregate(Operator input, List<String> groupBy, AggregateFunction function,
      String column, String alias) {
    if (column == null && function != AggregateFunction.COUNT) {
      throw new IllegalArgumentException(function + " demande une colonne");
    }
    this.input = input;
    this.groupBy = List.copyOf(groupBy);
    this.function = function;
    this.column = column;
    this.alias = alias;
  }

  @Override
  public Table execute() {
    Table in = input.execute();
    List<Integer> keys = groupBy.stream().map(in::getColumnIndex).toList();
    Function<Row, Object> value = column == null ? row -> row : columnValue(in);

    LinkedHashMap<List<Object>, List<Row>> groups = in.getRows().stream()
        .collect(Collectors.groupingBy(
            row -> keys.stream().map(row::get).toList(),
            LinkedHashMap::new,
            Collectors.toList()));

    List<Row> rows = groups.entrySet().stream()
        .map(e -> {
          Object result = function.apply(e.getValue().stream().map(value).toList());
          return new Row(Stream.concat(e.getKey().stream(), Stream.of(result)).toList());
        })
        .toList();
    List<String> columns = Stream.concat(groupBy.stream(), Stream.of(alias)).toList();
    return new Table(columns, rows);
  }

  private Function<Row, Object> columnValue(Table in) {
    int idx = in.getColumnIndex(column);
    return row -> row.get(idx);
  }
}
