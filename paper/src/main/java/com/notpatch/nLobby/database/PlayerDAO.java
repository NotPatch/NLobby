package com.notpatch.nLobby.database;

import com.notpatch.nLobby.model.LobbyPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDAO {
    private final DatabaseManager databaseManager;

    public PlayerDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public LobbyPlayer loadPlayer(UUID uuid) throws SQLException {
        String sql = "SELECT uuid, name, coins, level, playtime, first_join, last_join FROM nlobby_players WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new LobbyPlayer(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getLong("coins"),
                        rs.getInt("level"),
                        rs.getLong("playtime"),
                        rs.getLong("first_join"),
                        rs.getLong("last_join")
                );
            }
            return null;
        }
    }

    public void savePlayer(LobbyPlayer player) throws SQLException {
        if (playerExists(player.getUuid())) {
            updatePlayer(player);
        } else {
            insertPlayer(player);
        }
    }

    private void insertPlayer(LobbyPlayer player) throws SQLException {
        String sql = "INSERT INTO nlobby_players (uuid, name, coins, level, playtime, first_join, last_join) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.getUuid().toString());
            ps.setString(2, player.getName());
            ps.setLong(3, player.getCoins());
            ps.setInt(4, player.getLevel());
            ps.setLong(5, player.getPlaytime());
            ps.setLong(6, player.getFirstJoin());
            ps.setLong(7, player.getLastJoin());
            ps.executeUpdate();
        }
    }

    private void updatePlayer(LobbyPlayer player) throws SQLException {
        String sql = "UPDATE nlobby_players SET name = ?, coins = ?, level = ?, playtime = ?, last_join = ? WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.getName());
            ps.setLong(2, player.getCoins());
            ps.setInt(3, player.getLevel());
            ps.setLong(4, player.getPlaytime());
            ps.setLong(5, player.getLastJoin());
            ps.setString(6, player.getUuid().toString());
            ps.executeUpdate();
        }
    }

    public boolean playerExists(UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM nlobby_players WHERE uuid = ? LIMIT 1";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            return ps.executeQuery().next();
        }
    }

    public void addCoins(UUID uuid, long amount) throws SQLException {
        String sql = "UPDATE nlobby_players SET coins = coins + ? WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public void setLevel(UUID uuid, int level) throws SQLException {
        String sql = "UPDATE nlobby_players SET level = ? WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, level);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public void updatePlaytime(UUID uuid, long seconds) throws SQLException {
        String sql = "UPDATE nlobby_players SET playtime = playtime + ? WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seconds);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }
}
