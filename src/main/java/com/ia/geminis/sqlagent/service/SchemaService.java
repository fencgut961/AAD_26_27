package com.ia.geminis.sqlagent.service;

import com.ia.geminis.sqlagent.constants.SqlAgentConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    private final String schema;

    public SchemaService(JdbcTemplate jdbcTemplate) {
        Map<String, List<String>> tables = new LinkedHashMap<>();
        jdbcTemplate.queryForList(SqlAgentConstants.SCHEMA_QUERY, SqlAgentConstants.SCHEMA_NAME)
                .forEach(column -> {
                    String tableName = (String) column.get(SqlAgentConstants.TABLE_NAME);
                    tables.computeIfAbsent(tableName, ignored -> new ArrayList<>())
                            .add(describeColumn(column));
                });

        this.schema = tables.entrySet().stream()
                .map(table -> table.getKey() + SqlAgentConstants.TABLE_COLUMNS_SEPARATOR
                        + String.join(SqlAgentConstants.COLUMN_SEPARATOR, table.getValue()))
                .collect(Collectors.joining(SqlAgentConstants.TABLE_SEPARATOR));

        if (schema.isBlank()) {
            throw new IllegalStateException(
                    "No se encontraron tablas en el esquema "
                            + SqlAgentConstants.SCHEMA_NAME + " de PostgreSQL.");
        }
    }

    public String getSchema() {
        return schema;
    }

    private String describeColumn(Map<String, Object> column) {
        StringBuilder description = new StringBuilder()
                .append(column.get(SqlAgentConstants.COLUMN_NAME))
                .append(SqlAgentConstants.COLUMN_TYPE_SEPARATOR)
                .append(column.get(SqlAgentConstants.DATA_TYPE))
                .append(SqlAgentConstants.CLOSING_PARENTHESIS);

        if (SqlAgentConstants.NOT_NULL_VALUE.equals(column.get(SqlAgentConstants.IS_NULLABLE))) {
            description.append(SqlAgentConstants.NOT_NULL_SUFFIX);
        }
        if (Boolean.TRUE.equals(column.get(SqlAgentConstants.IS_PRIMARY_KEY))) {
            description.append(SqlAgentConstants.PRIMARY_KEY_SUFFIX);
        }
        if (column.get(SqlAgentConstants.FOREIGN_TABLE) != null) {
            description.append(SqlAgentConstants.REFERENCES_PREFIX)
                    .append(column.get(SqlAgentConstants.FOREIGN_TABLE))
                    .append(SqlAgentConstants.OPENING_PARENTHESIS)
                    .append(column.get(SqlAgentConstants.FOREIGN_COLUMN))
                    .append(SqlAgentConstants.CLOSING_PARENTHESIS);
        }
        return description.toString();
    }
}
