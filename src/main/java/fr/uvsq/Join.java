package fr.uvsq;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Équi-jointure entre deux tables.
 */
public class Join implements Operator {
  private final Operator left;
  private final Operator right;
  private final String leftCol;
  private final String rightCol;

  /**
   * Crée une jointure sur l'égalité leftCol = rightCol.
   */
  public Join(Operator left, Operator right, String leftCol, String rightCol) {
    this.left = left;
    this.right = right;
    this.leftCol = leftCol;
    this.rightCol = rightCol;
  }

  @Override
  public Table execute() {
    Table l = left.execute();
    Table r = right.execute();
    int leftIdx = l.getColumnIndex(leftCol);
    int rightIdx = r.getColumnIndex(rightCol);

    // colonnes de droite absentes à gauche
    List<Integer> keptRight = IntStream.range(0, r.getColumns().size())
        .filter(i -> !l.getColumns().contains(r.getColumns().get(i)))
        .boxed()
        .toList();
    List<String> columns = Stream.concat(l.getColumns().stream(),
        keptRight.stream().map(r.getColumns()::get)).toList();

    Map<Object, List<Row>> index = r.getRows().stream()
        .collect(Collectors.groupingBy(row -> row.get(rightIdx)));

    List<Row> rows = l.getRows().stream()
        .flatMap(lrow -> index.getOrDefault(lrow.get(leftIdx), List.of()).stream()
            .map(rrow -> new Row(Stream.concat(lrow.getValues().stream(),
                keptRight.stream().map(rrow::get)).toList())))
        .toList();
    return new Table(columns, rows);
  }
}
