package fr.uvsq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectTest {

    private Table users;

    @BeforeEach
    void setUp() {
        users = new Table(List.of("id", "name", "age"));
        users.addRow(new Row(List.of(1, "Alice", 22)));
        users.addRow(new Row(List.of(2, "Bob", 30)));
    }

    @Test
    void projectGardeUniquementLesColonnesDemandees() {
        Operator plan = new Project(new Scan(users), List.of("name"));
        Table result = plan.execute();

        assertEquals(List.of("name"), result.getColumns());
        assertEquals(2, result.getRows().size());
        assertEquals(List.of("Alice"), result.getRows().get(0).getValues());
        assertEquals(List.of("Bob"), result.getRows().get(1).getValues());
    }

    @Test
    void projectRespecteLOrdreDesColonnesDemande() {
        Operator plan = new Project(new Scan(users), List.of("age", "id"));
        Table result = plan.execute();

        assertEquals(List.of("age", "id"), result.getColumns());
        assertEquals(List.of(22, 1), result.getRows().get(0).getValues());
    }
}