package fr.uvsq;

import java.util.ArrayList;
import java.util.List;

/**
 * Projection sur une liste de colonnes.
 */
public class Project implements Operator {
  private final Operator input;
  private final List<String> columns;

  public Project(Operator input, List<String> columns) {
    this.input = input;
    this.columns = columns;
  }

  @Override
  public Table execute() {
    Table inTable = input.execute();
    Table outTable = new Table(columns);
    List<String> inCols = inTable.getColumns();
    for (Row row : inTable.getRows()) {
      List<Object> vals = new ArrayList<>();
      for (String c : columns) {
        vals.add(row.get(inCols.indexOf(c)));
      }
      outTable.addRow(new Row(vals));
    }
    return outTable;
  }
}
