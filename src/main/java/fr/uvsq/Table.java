package fr.uvsq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Table en mémoire : un schéma (noms de colonnes) et des lignes. */
public class Table {
  private final List<String> columns;
  private final List<Row> rows;

  /** Crée une table vide. */
  public Table(List<String> columns) {
    this(columns, List.of());
  }

  /** Crée une table avec ses lignes. */
  public Table(List<String> columns, List<Row> rows) {
    this.columns = List.copyOf(columns);
    this.rows = new ArrayList<>(rows);
  }

  /** Renvoie la position d'une colonne. */
  public int getColumnIndex(String colName) {
    int idx = columns.indexOf(colName);
    if (idx == -1) {
      throw new IllegalArgumentException("Colonne inconnue: " + colName);
    }
    return idx;
  }

  public List<String> getColumns() {
    return columns;
  }

  public List<Row> getRows() {
    return Collections.unmodifiableList(rows);
  }

  /** Ajoute une ligne à la fin de la table. */
  public void addRow(Row row) {
    rows.add(row);
  }
}
