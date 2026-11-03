package com.ia.geminis.sqlagent.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaServiceTest {

    @Test
    void buildsSchemaWithColumnTypesAndRelationships() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Map<String, Object> id = column("usuarios", "id", "integer", "NO", true, null, null);
        Map<String, Object> userId = column("ventas", "usuario_id", "integer", "NO", false, "usuarios", "id");
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(id, userId));

        SchemaService schemaService = new SchemaService(jdbcTemplate);

        assertEquals("""
                usuarios: id (integer) NOT NULL PRIMARY KEY
                ventas: usuario_id (integer) NOT NULL REFERENCES usuarios(id)""",
                schemaService.getSchema());
    }

    @Test
    void rejectsAnEmptySchema() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> new SchemaService(jdbcTemplate));
    }

    private Map<String, Object> column(String table, String name, String type, String nullable,
                                       boolean primaryKey, String foreignTable, String foreignColumn) {
        Map<String, Object> column = new HashMap<>();
        column.put("table_name", table);
        column.put("column_name", name);
        column.put("data_type", type);
        column.put("is_nullable", nullable);
        column.put("is_primary_key", primaryKey);
        column.put("foreign_table", foreignTable);
        column.put("foreign_column", foreignColumn);
        return column;
    }
}
