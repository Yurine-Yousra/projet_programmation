package fr.uvsq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Intersection de deux relations de même schéma.
 */
public class Intersection extends SetOperation {

  /** Crée l'intersection de left et right. */
  public Intersection(Operator left, Operator right) {
    super(left, right);
  }

  @Override
  protected List<Row> combine(List<Row> left, List<Row> right) {
    Set<Row> inRight = new HashSet<>(right);
    return left.stream().filter(inRight::contains).toList();
  }
}
