package fr.uvsq;

import java.util.List;

/** Sélection des lignes qui vérifient un prédicat. */
public class Select implements Operator {
  private final Operator input;
  private final PredicateParser.RowPredicate predicate;

  /** Crée une sélection sur le résultat de input. */
  public Select(Operator input, PredicateParser.RowPredicate predicate) {
    this.input = input;
    this.predicate = predicate;
  }

  @Override
  public Table execute() {
    Table in = input.execute();
    List<Row> rows = in.getRows().stream()
        .filter(row -> predicate.test(row, in))
        .toList();
    return new Table(in.getColumns(), rows);
  }
}
