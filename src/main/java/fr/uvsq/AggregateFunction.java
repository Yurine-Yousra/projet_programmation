package fr.uvsq;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.DoubleStream;

/**
 * Fonctions d'agrégation disponibles pour l'opérateur AGGREGATE.
 */
public enum AggregateFunction {
  COUNT {
    @Override
    Object apply(List<Object> values) {
      return values.size();
    }
  },
  SUM {
    @Override
    Object apply(List<Object> values) {
      if (values.stream().allMatch(v -> v instanceof Integer || v instanceof Long)) {
        return CsvUtils.toInteger(
            values.stream().mapToLong(v -> ((Number) v).longValue()).sum());
      }
      return numbers(values).sum();
    }
  },
  AVG {
    @Override
    Object apply(List<Object> values) {
      return numbers(values).average().orElseThrow();
    }
  },
  MIN {
    @Override
    Object apply(List<Object> values) {
      return values.stream().min(ORDER).orElseThrow();
    }
  },
  MAX {
    @Override
    Object apply(List<Object> values) {
      return values.stream().max(ORDER).orElseThrow();
    }
  };

  // nombres comparés par valeur, sinon ordre alphabétique
  private static final Comparator<Object> ORDER = (a, b) ->
      a instanceof Number x && b instanceof Number y
          ? Double.compare(x.doubleValue(), y.doubleValue())
          : a.toString().compareTo(b.toString());

  abstract Object apply(List<Object> values);

  /**
   * Renvoie la fonction correspondant au nom, sans tenir compte de la casse.
   *
   * @param name nom de la fonction, par exemple "sum"
   * @return la fonction
   * @throws IllegalArgumentException si le nom est inconnu
   */
  public static AggregateFunction of(String name) {
    try {
      return valueOf(name.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Fonction d'agrégation inconnue : " + name, e);
    }
  }

  private static DoubleStream numbers(List<Object> values) {
    return values.stream().mapToDouble(v -> {
      if (v instanceof Number n) {
        return n.doubleValue();
      }
      throw new IllegalArgumentException("Valeur non numérique : " + v);
    });
  }
}
