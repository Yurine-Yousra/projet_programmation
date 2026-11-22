package fr.uvsq;

import java.util.List;

/**
 * Ligne d'une table : liste immuable de valeurs.
 */
public class Row {
  private final List<Object> values;

  /** Crée une ligne à partir de ses valeurs. */
  public Row(List<Object> values) {
    this.values = List.copyOf(values);
  }

  /** Renvoie la valeur à la position donnée. */
  public Object get(int index) {
    return values.get(index);
  }

  /** Renvoie la valeur entière à la position donnée. */
  public int getInt(int index) {
    return (int) values.get(index);
  }

  /**
   * Renvoie la valeur en float si elle est décimale.
   *
   * @param index position de la valeur
   * @return la valeur convertie
   */
  public float getFloat(int index) {
    Object v = values.get(index);
    if (v instanceof Float f) {
      return f;
    }
    if (v instanceof Double d) {
      return d.floatValue();
    }
    throw new ClassCastException("Valeur non convertible en float: " + v);
  }

  /** Renvoie la chaîne à la position donnée. */
  public String getString(int index) {
    return (String) values.get(index);
  }

  public List<Object> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return values.toString();
  }
}
