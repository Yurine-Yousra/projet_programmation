package fr.uvsq;

/**
 * Sélection des lignes qui vérifient un prédicat.
 */
public class Select implements Operator {
  private final Operator input;
  private final PredicateParser.RowPredicate predicate;

  public Select(Operator input, PredicateParser.RowPredicate predicate) {
    this.input = input;
    this.predicate = predicate;
  }

  @Override
  public Table execute() {
    Table inputTable = input.execute();
    Table result = new Table(inputTable.getColumns());
    for (Row row : inputTable.getRows()) {
      if (predicate.test(row, inputTable)) {
        result.addRow(row);
      }
    }
    return result;
  }
}
