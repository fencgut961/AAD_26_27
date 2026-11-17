package com.example.postgresql;

import com.example.postgresql.config.PostgresqlDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class PostgresqlConnectionApplication implements CommandLineRunner {

    public final PostgresqlDriver postgresqlDriver;

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
        }
    }
}
