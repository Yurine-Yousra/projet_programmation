package fr.uvsq;

import java.util.Set;

/**
 * Analyse un prédicat simple de la forme "colonne opérateur valeur",
 * par exemple "age &gt; 30" ou "name = 'Bob'".
 */
public final class PredicateParser {

  private static final Set<String> OPERATORS =
      Set.of(">", "<", ">=", "<=", "=", "==", "!=", "<>");

  private PredicateParser() {
  }

  /**
   * Prédicat évalué sur une ligne de table.
   */
  @FunctionalInterface
  public interface RowPredicate {
    /** Indique si la ligne vérifie le prédicat. */
    boolean test(Row row, Table table);
  }

  /**
   * Transforme l'expression en prédicat.
   *
   * @param expr expression en trois parties séparées par des espaces
   * @return le prédicat correspondant
   */
  public static RowPredicate parse(String expr) {
    String[] parts = expr.trim().split("\\s+", 3);
    if (parts.length != 3) {
      throw new IllegalArgumentException("Prédicat invalide, doit avoir 3 parties : " + expr);
    }
    String col = parts[0];
    String op = parts[1];
    if (!OPERATORS.contains(op)) {
      throw new IllegalArgumentException("Opérateur inconnu : " + op);
    }
    String value = unquote(parts[2]);
    Double number = toNumber(value);

    return (row, table) -> {
      Object cell = row.get(table.getColumnIndex(col));
      Double left = toNumber(cell);
      if (left != null && number != null) {
        return compareNumbers(op, left, number);
      }
      return compareStrings(op, cell.toString(), value);
    };
  }

  private static String unquote(String s) {
    if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  private static Double toNumber(Object o) {
    if (o instanceof Number n) {
      return n.doubleValue();
    }
    try {
      return Double.parseDouble(o.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static boolean compareNumbers(String op, double left, double right) {
    return switch (op) {
      case ">" -> left > right;
      case "<" -> left < right;
      case ">=" -> left >= right;
      case "<=" -> left <= right;
      case "=", "==" -> left == right;
      case "!=", "<>" -> left != right;
      default -> throw new IllegalStateException(op);
    };
  }

  private static boolean compareStrings(String op, String left, String right) {
    return switch (op) {
      case "=", "==" -> left.equals(right);
      case "!=", "<>" -> !left.equals(right);
      default -> throw new IllegalArgumentException(
          "Opérateur " + op + " non valide sur des chaînes");
    };
  }
}
