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
   *
   * @param filename chemin du fichier
   * @return la table lue
   * @throws IOException en cas d'erreur de lecture
   */
  public static Table readCsv(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));
    if (lines.isEmpty()) {
      return new Table(List.of());
    }
    List<String> columns = List.of(lines.get(0).split(","));
    List<Row> rows = lines.stream()
        .skip(1)
        .map(line -> new Row(Arrays.stream(line.split(","))
            .map(CsvUtils::parseValue)
            .toList()))
        .toList();
    return new Table(columns, rows);
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
