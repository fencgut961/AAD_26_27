package com.ia.geminis.sqlagent.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SqlAgentConstants {
    public static final String API_BASE_PATH = "/api/agente";
    public static final String ASK_PATH = "/preguntar";
    public static final int MAX_MESSAGE_LENGTH = 4_000;

    public static final String SELECT_KEYWORD = "SELECT";
    public static final String STATEMENT_SEPARATOR = ";";

    public static final String SCHEMA_NAME = "public";
    public static final String TABLE_NAME = "table_name";
    public static final String COLUMN_NAME = "column_name";
    public static final String DATA_TYPE = "data_type";
    public static final String IS_NULLABLE = "is_nullable";
    public static final String IS_PRIMARY_KEY = "is_primary_key";
    public static final String FOREIGN_TABLE = "foreign_table";
    public static final String FOREIGN_COLUMN = "foreign_column";
    public static final String NOT_NULL_VALUE = "NO";
    public static final String NOT_NULL_SUFFIX = " NOT NULL";
    public static final String PRIMARY_KEY_SUFFIX = " PRIMARY KEY";
    public static final String REFERENCES_PREFIX = " REFERENCES ";
    public static final String COLUMN_TYPE_SEPARATOR = " (";
    public static final String OPENING_PARENTHESIS = "(";
    public static final String CLOSING_PARENTHESIS = ")";
    public static final String TABLE_SEPARATOR = "\n";
    public static final String TABLE_COLUMNS_SEPARATOR = ": ";
    public static final String COLUMN_SEPARATOR = ", ";

    public static final String SCHEMA_QUERY = """
            SELECT
                c.table_name AS table_name,
                c.column_name AS column_name,
                c.data_type AS data_type,
                c.is_nullable AS is_nullable,
                pk.column_name IS NOT NULL AS is_primary_key,
                fk.foreign_table AS foreign_table,
                fk.foreign_column AS foreign_column
            FROM information_schema.columns c
            LEFT JOIN (
                SELECT k.table_schema, k.table_name, k.column_name
                FROM information_schema.key_column_usage k
                JOIN information_schema.table_constraints t
                    USING (constraint_catalog, constraint_schema, constraint_name, table_schema, table_name)
                WHERE t.constraint_type = 'PRIMARY KEY'
            ) pk USING (table_schema, table_name, column_name)
            LEFT JOIN (
                SELECT
                    k.table_schema,
                    k.table_name,
                    k.column_name,
                    ref.table_name AS foreign_table,
                    ref.column_name AS foreign_column
                FROM information_schema.key_column_usage k
                JOIN information_schema.table_constraints t
                    USING (constraint_catalog, constraint_schema, constraint_name, table_schema, table_name)
                JOIN information_schema.constraint_column_usage ref
                    USING (constraint_catalog, constraint_schema, constraint_name)
                WHERE t.constraint_type = 'FOREIGN KEY'
                    AND k.position_in_unique_constraint = ref.ordinal_position
            ) fk USING (table_schema, table_name, column_name)
            WHERE c.table_schema = ?
            ORDER BY c.table_name, c.ordinal_position
            """;

    public static final String SYSTEM_PROMPT = """
            Eres un tutor pedagógico y analista experto en bases de datos PostgreSQL.

            Esquema disponible (leído al iniciar la aplicación):
            %s

            Reglas:
            1. Formula consultas SELECT para responder a la petición y limita los resultados cuando sea apropiado.
            2. Usa la herramienta de consulta para obtener los datos antes de responder.
            3. Explica la respuesta de forma clara y pedagógica e incluye la consulta utilizada.
            4. No solicites ni intentes modificar datos o la estructura de la base de datos.
            """;

}
