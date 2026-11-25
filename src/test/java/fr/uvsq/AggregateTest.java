package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AggregateTest {

  private Table orders;

  @BeforeEach
  void setUp() {
    orders = new Table(List.of("id", "user_id", "amount"));
    orders.addRow(new Row(List.of(10, 2, 100)));
    orders.addRow(new Row(List.of(11, 1, 150)));
    orders.addRow(new Row(List.of(12, 2, 200)));
    orders.addRow(new Row(List.of(13, 3, 50)));
  }

  private Table aggregate(List<String> groupBy, AggregateFunction f, String col) {
    return new Aggregate(new Scan(orders), groupBy, f, col, "res").execute();
  }

  @Test
  void sumParGroupeGardeOrdreDesGroupes() {
    Table result = aggregate(List.of("user_id"), AggregateFunction.SUM, "amount");

    assertEquals(List.of("user_id", "res"), result.getColumns());
    assertEquals(List.of(
        new Row(List.of(2, 300)),
        new Row(List.of(1, 150)),
        new Row(List.of(3, 50))), result.getRows());
  }

  @Test
  void countSansGroupeCompteToutesLesLignes() {
    Table result = aggregate(List.of(), AggregateFunction.COUNT, null);

    assertEquals(List.of("res"), result.getColumns());
    assertEquals(List.of(new Row(List.of(4))), result.getRows());
  }

  @Test
  void avgRenvoieUnDecimal() {
    Table result = aggregate(List.of(), AggregateFunction.AVG, "amount");

    assertEquals(125.0, result.getRows().get(0).get(0));
  }

  @Test
  void minEtMaxParGroupe() {
    Table min = aggregate(List.of("user_id"), AggregateFunction.MIN, "amount");
    Table max = aggregate(List.of("user_id"), AggregateFunction.MAX, "amount");

    assertEquals(100, min.getRows().get(0).get(1));
    assertEquals(200, max.getRows().get(0).get(1));
  }

  @Test
  void maxFonctionneSurDesChaines() {
    Table users = new Table(List.of("name"));
    users.addRow(new Row(List.of("Bob")));
    users.addRow(new Row(List.of("Alice")));

    Table result = new Aggregate(new Scan(users), List.of(), AggregateFunction.MAX,
        "name", "res").execute();

    assertEquals("Bob", result.getRows().get(0).get(0));
  }

  @Test
  void sumSurUneChaineEstRefuse() {
    Table users = new Table(List.of("name"));
    users.addRow(new Row(List.of("Bob")));
    Aggregate op = new Aggregate(new Scan(users), List.of(), AggregateFunction.SUM,
        "name", "res");

    assertThrows(IllegalArgumentException.class, op::execute);
  }

  @Test
  void sumSansColonneEstRefuse() {
    assertThrows(IllegalArgumentException.class,
        () -> new Aggregate(new Scan(orders), List.of(), AggregateFunction.SUM, null, "res"));
  }

  @Test
  void fonctionInconnueEstRefusee() {
    assertThrows(IllegalArgumentException.class, () -> AggregateFunction.of("mediane"));
    assertEquals(AggregateFunction.SUM, AggregateFunction.of("sum"));
  }

  @Test
  void colonneInconnueEstRefusee() {
    assertThrows(IllegalArgumentException.class,
        () -> aggregate(List.of("pays"), AggregateFunction.COUNT, null));
  }

  @Test
  void countSurTableVideRenvoieZero() {
    Scan vide = new Scan(new Table(List.of("id")));

    Table result = new Aggregate(vide, List.of(), AggregateFunction.COUNT, null, "res").execute();

    assertEquals(List.of(new Row(List.of(0))), result.getRows());
  }
}
