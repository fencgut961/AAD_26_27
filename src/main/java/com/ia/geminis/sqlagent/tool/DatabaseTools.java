package com.ia.geminis.sqlagent.tool;

import com.ia.geminis.sqlagent.constants.SqlAgentConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseTools {

    private final JdbcTemplate jdbcTemplate;

    @Tool(description = "Ejecuta una consulta SELECT de solo lectura en PostgreSQL.")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> runSqlQuery(String query) {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("La consulta no puede estar vacía.");
        }

        String sql = query.trim();
        if (!StringUtils.startsWithIgnoreCase(sql, SqlAgentConstants.SELECT_KEYWORD)
                || (sql.length() > SqlAgentConstants.SELECT_KEYWORD.length()
                && Character.isJavaIdentifierPart(sql.charAt(SqlAgentConstants.SELECT_KEYWORD.length())))
                || sql.contains(SqlAgentConstants.STATEMENT_SEPARATOR)) {
            throw new IllegalArgumentException("Solo se permite una consulta SELECT.");
        }

        return jdbcTemplate.queryForList(sql);
    }
}
