package fr.uvsq;

/** Lecture d'une table chargée en mémoire. */
public class Scan implements Operator {
  private final Table table;

  /** Crée un scan de la table donnée. */
  public Scan(Table table) {
    this.table = table;
  }

  @Override
  public Table execute() {
    return table;
  }
}
