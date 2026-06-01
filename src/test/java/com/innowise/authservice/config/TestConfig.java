package com.innowise.authservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration
public class TestConfig {
    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgreSQLContainer(){
        return new PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("testDb")
                .withPassword("test")
                .withUsername("test");
    }
}
