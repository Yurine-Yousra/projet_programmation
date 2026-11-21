package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JoinTest {

    private Table users;
    private Table orders;

    @BeforeEach
    void setUp() {
        users = new Table(List.of("id", "name"));
        users.addRow(new Row(List.of(1, "Alice")));
        users.addRow(new Row(List.of(2, "Bob")));
        users.addRow(new Row(List.of(3, "Charlie")));

        orders = new Table(List.of("order_id", "user_id", "amount"));
        orders.addRow(new Row(List.of(10, 1, 50)));
        orders.addRow(new Row(List.of(11, 2, 30)));
        orders.addRow(new Row(List.of(12, 1, 20)));
    }

    @Test
    void joinFusionneLesSchemas() {
        Operator plan = new Join(new Scan(users), new Scan(orders), "id", "user_id");
        Table result = plan.execute();

        assertEquals(List.of("id", "name", "order_id", "user_id", "amount"), result.getColumns());
    }

    @Test
    void joinAssocieChaqueUtilisateurASesCommandes() {
        Operator plan = new Join(new Scan(users), new Scan(orders), "id", "user_id");
        Table result = plan.execute();

        assertEquals(3, result.getRows().size());
        assertEquals(List.of(1, "Alice", 10, 1, 50), result.getRows().get(0).getValues());
        assertEquals(List.of(1, "Alice", 12, 1, 20), result.getRows().get(1).getValues());
        assertEquals(List.of(2, "Bob", 11, 2, 30), result.getRows().get(2).getValues());
    }
}