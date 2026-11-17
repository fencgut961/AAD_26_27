package com.example.postgresql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}