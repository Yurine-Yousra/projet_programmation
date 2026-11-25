package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvUtilsTest {

  @TempDir
  Path dir;

  @Test
  void lectureConvertitLesTypes() throws IOException {
    Path csv = dir.resolve("t.csv");
    Files.write(csv, List.of("id,name,score", "1,Alice,12.5"));

    Table t = CsvUtils.readCsv(csv.toString());

    assertEquals(List.of("id", "name", "score"), t.getColumns());
    assertEquals(List.of(1, "Alice", 12.5), t.getRows().get(0).getValues());
  }

  @Test
  void fichierVideDonneUneTableVide() throws IOException {
    Path csv = dir.resolve("vide.csv");
    Files.write(csv, List.of());

    assertTrue(CsvUtils.readCsv(csv.toString()).getColumns().isEmpty());
  }

  @Test
  void espacesEtLignesVidesSontIgnores() throws IOException {
    Path csv = dir.resolve("sale.csv");
    Files.write(csv, List.of("id, name", "1 , Alice", "", "2,Bob "));

    Table t = CsvUtils.readCsv(csv.toString());

    assertEquals(List.of("id", "name"), t.getColumns());
    assertEquals(List.of(new Row(List.of(1, "Alice")), new Row(List.of(2, "Bob"))),
        t.getRows());
  }

  @Test
  void valeurVideEnFinDeLigneEstConservee() throws IOException {
    Path csv = dir.resolve("vide_fin.csv");
    Files.write(csv, List.of("id,name", "1,"));

    assertEquals(List.of(1, ""), CsvUtils.readCsv(csv.toString()).getRows().get(0).getValues());
  }

  @Test
  void ligneAvecMauvaisNombreDeValeursEstRefusee() throws IOException {
    Path csv = dir.resolve("bancal.csv");
    Files.write(csv, List.of("id,name", "1,Alice,22"));

    assertThrows(IllegalArgumentException.class, () -> CsvUtils.readCsv(csv.toString()));
  }

  @Test
  void ecritureSansEspaceNiGuillemet() throws IOException {
    Table t = new Table(List.of("id", "name"));
    t.addRow(new Row(List.of(1, "Alice")));
    Path out = dir.resolve("sous/dossier/out.csv");

    CsvUtils.writeCsv(out.toString(), t);

    assertEquals(List.of("id,name", "1,Alice"), Files.readAllLines(out));
  }

  @Test
  void lectureEtEcritureSontSymetriques() throws IOException {
    Table users = CsvUtils.readCsv("data/users.csv");
    Path out = dir.resolve("users.csv");

    CsvUtils.writeCsv(out.toString(), users);

    assertEquals(Files.readAllLines(Path.of("data/users.csv")), Files.readAllLines(out));
  }

  @Test
  void grandEntierEstLu() {
    assertEquals(12345678901L, CsvUtils.parseValue("12345678901"));
    assertEquals(-5, CsvUtils.parseValue("-5"));
  }
}
