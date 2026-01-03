package com.shieldauth.database;

import com.shieldauth.ShieldAuth;
import com.shieldauth.utils.EncryptionUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final ShieldAuth plugin;
    private HikariDataSource dataSource;
    private EncryptionUtil.EncryptionType encryptionType;

    public DatabaseManager(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String type = plugin.getConfigManager().getConfig().getString("database.type", "sqlite");
        String encryption = plugin.getConfigManager().getConfig().getString("security.encryption", "ARGON2");
        encryptionType = EncryptionUtil.fromString(encryption);

        HikariConfig config = new HikariConfig();
        config.setPoolName("ShieldAuth-Pool");
        config.setMaximumPoolSize(plugin.getConfigManager().getConfig().getInt("database.pool-size", 10));
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);

        if (type.equalsIgnoreCase("mysql")) {
            String host = plugin.getConfigManager().getConfig().getString("database.host", "localhost");
            int port = plugin.getConfigManager().getConfig().getInt("database.port", 3306);
            String database = plugin.getConfigManager().getConfig().getString("database.database", "shieldauth");
            String username = plugin.getConfigManager().getConfig().getString("database.username", "root");
            String password = plugin.getConfigManager().getConfig().getString("database.password", "");

            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setConnectionTestQuery("SELECT 1");
        }

        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS shieldauth_users (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16) NOT NULL, " +
                "password TEXT NOT NULL, " +
                "pin TEXT, " +
                "email TEXT, " +
                "email_verified INTEGER DEFAULT 0, " +
                "email_code TEXT, " +
                "last_ip TEXT, " +
                "last_login BIGINT, " +
                "register_date BIGINT, " +
                "register_ip TEXT" +
                ")";

        String createSessionsTable = "CREATE TABLE IF NOT EXISTS shieldauth_sessions (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "ip TEXT NOT NULL, " +
                "timestamp BIGINT NOT NULL" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createSessionsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
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

    public void reload() {
        close();
        initialize();
    }

    public boolean isRegistered(UUID uuid) {
        String query = "SELECT uuid FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check registration: " + e.getMessage());
            return false;
        }
    }

    public boolean isRegisteredByName(String username) {
        String query = "SELECT uuid FROM shieldauth_users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check registration by name: " + e.getMessage());
            return false;
        }
    }

    public boolean register(UUID uuid, String username, String password, String ip) {
        String hashedPassword = EncryptionUtil.hash(password, encryptionType);
        String query = "INSERT INTO shieldauth_users (uuid, username, password, register_date, register_ip, last_ip) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setString(5, ip);
            stmt.setString(6, ip);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to register user: " + e.getMessage());
            return false;
        }
    }

    public boolean login(UUID uuid, String password) {
        String query = "SELECT password FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                return EncryptionUtil.verify(password, storedHash, encryptionType);
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to verify login: " + e.getMessage());
            return false;
        }
    }

    public void updateLastLogin(UUID uuid, String ip) {
        String query = "UPDATE shieldauth_users SET last_login = ?, last_ip = ? WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, ip);
            stmt.setString(3, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update last login: " + e.getMessage());
        }
    }

    public boolean unregister(UUID uuid) {
        String query = "DELETE FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            int affected = stmt.executeUpdate();
            deleteSession(uuid);
            return affected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to unregister user: " + e.getMessage());
            return false;
        }
    }

    public boolean changePassword(UUID uuid, String currentPassword, String newPassword) {
        if (!login(uuid, currentPassword)) {
            return false;
        }
        String hashedPassword = EncryptionUtil.hash(newPassword, encryptionType);
        String query = "UPDATE shieldauth_users SET password = ? WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to change password: " + e.getMessage());
            return false;
        }
    }

    public boolean setPin(UUID uuid, String pin) {
        String hashedPin = EncryptionUtil.hashPin(pin);
        String query = "UPDATE shieldauth_users SET pin = ? WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPin);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set pin: " + e.getMessage());
            return false;
        }
    }

    public boolean removePin(UUID uuid) {
        String query = "UPDATE shieldauth_users SET pin = NULL WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove pin: " + e.getMessage());
            return false;
        }
    }

    public boolean hasPin(UUID uuid) {
        String query = "SELECT pin FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String pin = rs.getString("pin");
                return pin != null && !pin.isEmpty();
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check pin: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyPin(UUID uuid, String pin) {
        String query = "SELECT pin FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPin = rs.getString("pin");
                return EncryptionUtil.verifyPin(pin, storedPin);
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to verify pin: " + e.getMessage());
            return false;
        }
    }

    public boolean setEmail(UUID uuid, String email, String code) {
        String query = "UPDATE shieldauth_users SET email = ?, email_verified = 0, email_code = ? WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, code);
            stmt.setString(3, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set email: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyEmail(UUID uuid, String code) {
        String query = "SELECT email_code FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedCode = rs.getString("email_code");
                if (storedCode != null && java.security.MessageDigest.isEqual(
                        storedCode.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        code.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                    String updateQuery = "UPDATE shieldauth_users SET email_verified = 1, email_code = NULL WHERE uuid = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, uuid.toString());
                        updateStmt.executeUpdate();
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to verify email: " + e.getMessage());
            return false;
        }
    }

    public boolean isEmailVerified(UUID uuid) {
        String query = "SELECT email_verified FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("email_verified") == 1;
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check email verification: " + e.getMessage());
            return false;
        }
    }

    public void saveSession(UUID uuid, String ip) {
        String query = "REPLACE INTO shieldauth_sessions (uuid, ip, timestamp) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, ip);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save session: " + e.getMessage());
        }
    }

    public boolean hasValidSession(UUID uuid, String ip) {
        if (!plugin.getConfigManager().getConfig().getBoolean("security.session-enabled", true)) {
            return false;
        }
        int timeout = plugin.getConfigManager().getConfig().getInt("security.session-timeout", 1800) * 1000;
        String query = "SELECT timestamp FROM shieldauth_sessions WHERE uuid = ? AND ip = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, ip);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long timestamp = rs.getLong("timestamp");
                return (System.currentTimeMillis() - timestamp) < timeout;
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check session: " + e.getMessage());
            return false;
        }
    }

    public void deleteSession(UUID uuid) {
        String query = "DELETE FROM shieldauth_sessions WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete session: " + e.getMessage());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        String query = "SELECT * FROM shieldauth_users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PlayerData data = new PlayerData();
                data.uuid = uuid;
                data.username = rs.getString("username");
                data.hasPin = rs.getString("pin") != null && !rs.getString("pin").isEmpty();
                data.email = rs.getString("email");
                data.emailVerified = rs.getInt("email_verified") == 1;
                data.lastLogin = rs.getLong("last_login");
                data.lastIp = rs.getString("last_ip");
                data.registerDate = rs.getLong("register_date");
                data.registerIp = rs.getString("register_ip");
                return data;
            }
            return null;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get player data: " + e.getMessage());
            return null;
        }
    }

    public PlayerData getPlayerDataByName(String username) {
        String query = "SELECT * FROM shieldauth_users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PlayerData data = new PlayerData();
                data.uuid = UUID.fromString(rs.getString("uuid"));
                data.username = rs.getString("username");
                data.hasPin = rs.getString("pin") != null && !rs.getString("pin").isEmpty();
                data.email = rs.getString("email");
                data.emailVerified = rs.getInt("email_verified") == 1;
                data.lastLogin = rs.getLong("last_login");
                data.lastIp = rs.getString("last_ip");
                data.registerDate = rs.getLong("register_date");
                data.registerIp = rs.getString("register_ip");
                return data;
            }
            return null;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get player data by name: " + e.getMessage());
            return null;
        }
    }

    public static class PlayerData {
        public UUID uuid;
        public String username;
        public boolean hasPin;
        public String email;
        public boolean emailVerified;
        public long lastLogin;
        public String lastIp;
        public long registerDate;
        public String registerIp;
    }
}
