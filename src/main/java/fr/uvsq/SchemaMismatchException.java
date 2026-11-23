package fr.uvsq;

import java.util.List;

/**
 * Levée quand deux relations n'ont pas le même schéma.
 */
public class SchemaMismatchException extends RuntimeException {

  /** Crée l'exception à partir des deux schémas incompatibles. */
  public SchemaMismatchException(List<String> left, List<String> right) {
    super("Schémas incompatibles : " + left + " et " + right);
  }
}
