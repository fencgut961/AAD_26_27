package com.example.postgresql;

import com.example.postgresql.config.PostgresqlDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class PostgresqlConnectionApplication implements CommandLineRunner {

    public final PostgresqlDriver postgresqlDriver;

    @Value("classpath:01_schema.sql")
    private Resource schemaScript;

    @Value("classpath:02_procedures.sql")
    private Resource proceduresScript;

    public static void main(String[] args) {
        SpringApplication.run(PostgresqlConnectionApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = postgresqlDriver.getConnection()) {
            log.info("Connection is valid: {}", conn.isValid(5));

            DatabaseMetaData metaData = conn.getMetaData();
            log.info("URL: {}", metaData.getURL());
            log.info("User: {}", metaData.getUserName());
            log.info("Product: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            log.info("Driver: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
            log.info("JDBC version: {}.{}", metaData.getJDBCMajorVersion(), metaData.getJDBCMinorVersion());
            log.info("Catalog: {}, Schema: {}", conn.getCatalog(), conn.getSchema());
            log.info("Read only: {}", metaData.isReadOnly());

            executeSqlScript(conn, schemaScript);
            executeSqlScript(conn, proceduresScript);
        }
    }

    private void executeSqlScript(Connection conn, Resource resource) throws IOException, SQLException {
        String script = resource.getContentAsString(StandardCharsets.UTF_8);
        try (Statement st = conn.createStatement()) {
            st.execute(script);
        }
        log.info("SQL script {} executed", resource.getFilename());
    }
}
