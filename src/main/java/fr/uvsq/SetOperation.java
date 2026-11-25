package fr.uvsq;

import java.util.List;

/**
 * Base des opérateurs ensemblistes : les deux relations doivent avoir
 * le même schéma et le résultat ne contient pas de doublons.
 */
public abstract class SetOperation implements Operator {
  private final Operator left;
  private final Operator right;

  /** Crée l'opération à partir de ses deux entrées. */
  protected SetOperation(Operator left, Operator right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Table execute() {
    Table l = left.execute();
    Table r = right.execute();
    if (!l.getColumns().equals(r.getColumns())) {
      throw new SchemaMismatchException(l.getColumns(), r.getColumns());
    }
    List<Row> rows = combine(l.getRows(), r.getRows())
        .stream()
        .distinct()
        .toList();
    return new Table(l.getColumns(), rows);
  }

  /** Calcule les lignes du résultat, éventuellement avec des doublons. */
  protected abstract List<Row> combine(List<Row> left, List<Row> right);
}
