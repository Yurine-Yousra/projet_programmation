package fr.uvsq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lecture et écriture de tables au format CSV.
 */
public final class CsvUtils {

  private CsvUtils() {
  }

  /**
   * Écrit la table dans un fichier CSV avec une ligne d'en-tête.
   *
   * @param filename chemin du fichier
   * @param table table à écrire
   * @throws IOException en cas d'erreur d'écriture
   */
  public static void writeCsv(String filename, Table table) throws IOException {
    Stream<String> header = Stream.of(String.join(",", table.getColumns()));
    Stream<String> body = table.getRows().stream()
        .map(row -> row.getValues().stream().map(Object::toString)
            .collect(Collectors.joining(",")));
    Path path = Path.of(filename);
    if (path.getParent() != null) {
      Files.createDirectories(path.getParent());
    }
    Files.write(path, Stream.concat(header, body).toList());
  }

  /**
   * Lit un fichier CSV dont la première ligne donne les colonnes.
   * Les lignes vides sont ignorées et les espaces autour des valeurs retirés.
   *
   * @param filename chemin du fichier
   * @return la table lue
   * @throws IOException en cas d'erreur de lecture
   * @throws IllegalArgumentException si une ligne n'a pas le bon nombre de valeurs
   */
  public static Table readCsv(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename)).stream()
        .filter(line -> !line.isBlank())
        .toList();
    if (lines.isEmpty()) {
      return new Table(List.of());
    }
    List<String> columns = split(lines.get(0));
    List<Row> rows = lines.stream()
        .skip(1)
        .map(line -> toRow(line, columns.size()))
        .toList();
    return new Table(columns, rows);
  }

  private static Row toRow(String line, int width) {
    List<String> values = split(line);
    if (values.size() != width) {
      throw new IllegalArgumentException(
          "Ligne avec " + values.size() + " valeurs au lieu de " + width + " : " + line);
    }
    return new Row(values.stream().map(CsvUtils::parseValue).toList());
  }

  private static List<String> split(String line) {
    return Arrays.stream(line.split(",", -1)).map(String::trim).toList();
  }

  static Object parseValue(String s) {
    if (s.matches("-?\\d+")) {
      return Integer.parseInt(s);
    }
    if (s.matches("-?\\d*\\.\\d+")) {
      return Double.parseDouble(s);
    }
    return s;
  }
}
