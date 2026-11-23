package fr.uvsq;

import java.util.List;
import java.util.stream.Stream;

/**
 * Union de deux relations de même schéma.
 */
public class Union extends SetOperation {

  /** Crée l'union de left et right. */
  public Union(Operator left, Operator right) {
    super(left, right);
  }

  @Override
  protected List<Row> combine(List<Row> left, List<Row> right) {
    return Stream.concat(left.stream(), right.stream()).toList();
  }
}
