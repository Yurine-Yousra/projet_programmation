package fr.uvsq;

import java.util.List;

/**
 * Projection sur une liste de colonnes.
 */
public class Project implements Operator {
  private final Operator input;
  private final List<String> columns;

  /** Crée une projection sur les colonnes données. */
  public Project(Operator input, List<String> columns) {
    this.input = input;
    this.columns = List.copyOf(columns);
  }

  @Override
  public Table execute() {
    Table in = input.execute();
    List<Integer> indexes = columns.stream().map(in::getColumnIndex).toList();
    List<Row> rows = in.getRows().stream()
        .map(row -> new Row(indexes.stream().map(row::get).toList()))
        .toList();
    return new Table(columns, rows);
  }
}
