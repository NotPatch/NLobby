package com.notpatch.nLobby.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.notpatch.nLobby.config.ConfigManager;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final ConfigManager configManager;
    @Getter
    private HikariDataSource dataSource;

    public DatabaseManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void init() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(configManager.getDatabasePoolSize());
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        String url = "jdbc:sqlite:nlobby.db";
        config.setJdbcUrl(url);

        this.dataSource = new HikariDataSource(config);
        initializeTables();
    }

    private void initializeTables() throws SQLException {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS nlobby_players (" +
                    "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "name VARCHAR(16) NOT NULL," +
                    "coins BIGINT NOT NULL DEFAULT 0," +
                    "level INT NOT NULL DEFAULT 1," +
                    "playtime BIGINT NOT NULL DEFAULT 0," +
                    "first_join TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "last_join TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
