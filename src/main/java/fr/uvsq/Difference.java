package fr.uvsq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Différence de deux relations de même schéma.
 */
public class Difference extends SetOperation {

  /** Crée la différence left moins right. */
  public Difference(Operator left, Operator right) {
    super(left, right);
  }

  @Override
  protected List<Row> combine(List<Row> left, List<Row> right) {
    Set<Row> inRight = new HashSet<>(right);
    return left.stream().filter(row -> !inRight.contains(row)).toList();
  }
}
