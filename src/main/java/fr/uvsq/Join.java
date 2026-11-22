package fr.uvsq;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    Table leftTable = left.execute();
    Table rightTable = right.execute();

    List<String> mergedCols = new ArrayList<>(leftTable.getColumns());
    for (String c : rightTable.getColumns()) {
      if (!mergedCols.contains(c)) {
        mergedCols.add(c);
      }
    }
    Table outTable = new Table(mergedCols);

    int leftIdx = leftTable.getColumns().indexOf(leftCol);
    int rightIdx = rightTable.getColumns().indexOf(rightCol);
    for (Row lrow : leftTable.getRows()) {
      for (Row rrow : rightTable.getRows()) {
        if (Objects.equals(lrow.get(leftIdx), rrow.get(rightIdx))) {
          List<Object> mergedVals = new ArrayList<>(lrow.getValues());
          for (int i = 0; i < rightTable.getColumns().size(); i++) {
            if (!leftTable.getColumns().contains(rightTable.getColumns().get(i))) {
              mergedVals.add(rrow.get(i));
            }
          }
          outTable.addRow(new Row(mergedVals));
        }
      }
    }
    return outTable;
  }
}
