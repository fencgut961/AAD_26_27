package com.ia.geminis.sqlagent.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class DatabaseToolsTest {

    private JdbcTemplate jdbcTemplate;
    private DatabaseTools databaseTools;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        databaseTools = new DatabaseTools(jdbcTemplate);
    }

    @Test
    void rejectsCommandsOtherThanSelect() {
        assertThrows(IllegalArgumentException.class,
                () -> databaseTools.runSqlQuery("DELETE FROM usuarios"));
        assertThrows(IllegalArgumentException.class,
                () -> databaseTools.runSqlQuery("WITH deleted AS (DELETE FROM usuarios) SELECT * FROM deleted"));
        assertThrows(IllegalArgumentException.class,
                () -> databaseTools.runSqlQuery("-- comentario\nSELECT 1"));

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void rejectsMultipleStatements() {
        assertThrows(IllegalArgumentException.class,
                () -> databaseTools.runSqlQuery("SELECT 1; DROP TABLE usuarios"));

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void rejectsEmptyQueries() {
        assertThrows(IllegalArgumentException.class,
                () -> databaseTools.runSqlQuery("   "));

        verifyNoInteractions(jdbcTemplate);
    }
}
