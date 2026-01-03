package com.shieldauth;

import com.shieldauth.commands.*;
import com.shieldauth.database.DatabaseManager;
import com.shieldauth.listeners.*;
import com.shieldauth.managers.*;
import com.shieldauth.utils.ConfigManager;
import com.shieldauth.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShieldAuth extends JavaPlugin {

    private static ShieldAuth instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private SessionManager sessionManager;
    private PinManager pinManager;
    private EmailManager emailManager;
    private DiscordWebhook discordWebhook;
    private UpdateChecker updateChecker;
    private CommandAliasListener commandAliasListener;
    private final Set<UUID> authenticatedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pinVerifiedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lockedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> pinAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pinLockedPlayers = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> ipLockedList = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipPinAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> ipPinLockedList = new ConcurrentHashMap<>();
    private final Set<String> adminList = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        sessionManager = new SessionManager(this);
        pinManager = new PinManager(this);
        emailManager = new EmailManager(this);
        discordWebhook = new DiscordWebhook(this);
        
        loadAdminList();
        
        registerCommands();
        registerListeners();
        
        updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates();
        
        Bukkit.getConsoleSender().sendMessage(configManager.getColoredMessage("messages.plugin-enabled"));
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        saveAdminList();
        Bukkit.getConsoleSender().sendMessage(configManager.getColoredMessage("messages.plugin-disabled"));
    }

    private void registerCommands() {
        EmptyTabCompleter emptyTab = new EmptyTabCompleter();
        
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("register").setTabCompleter(emptyTab);
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("login").setTabCompleter(emptyTab);
        getCommand("unregister").setExecutor(new UnregisterCommand(this));
        getCommand("unregister").setTabCompleter(emptyTab);
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("changepassword").setTabCompleter(emptyTab);
        getCommand("setpin").setExecutor(new SetPinCommand(this));
        getCommand("setpin").setTabCompleter(emptyTab);
        getCommand("removepin").setExecutor(new RemovePinCommand(this));
        getCommand("removepin").setTabCompleter(emptyTab);
        getCommand("unsetpin").setExecutor(new UnsetPinCommand(this));
        getCommand("unsetpin").setTabCompleter(emptyTab);
        getCommand("setemail").setExecutor(new SetEmailCommand(this));
        getCommand("setemail").setTabCompleter(emptyTab);
        getCommand("verifyemail").setExecutor(new VerifyEmailCommand(this));
        getCommand("verifyemail").setTabCompleter(emptyTab);
        getCommand("shieldauth").setExecutor(new ShieldAuthCommand(this));
        getCommand("shieldauth").setTabCompleter(new ShieldAuthTabCompleter(this));
    }

    private void registerListeners() {
        commandAliasListener = new CommandAliasListener(this);
        getServer().getPluginManager().registerEvents(commandAliasListener, this);
        getServer().getPluginManager().registerEvents(new PlayerPreLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
    }

    private void loadAdminList() {
        adminList.clear();
        List<String> admins = configManager.getConfig().getStringList("admin-list");
        adminList.addAll(admins);
    }

    public void saveAdminList() {
        configManager.getConfig().set("admin-list", new ArrayList<>(adminList));
        configManager.saveConfig();
    }

    public void reloadPlugin() {
        configManager.loadConfig();
        loadAdminList();
        databaseManager.reload();
        if (commandAliasListener != null) {
            commandAliasListener.reload();
        }
    }

    public static ShieldAuth getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public PinManager getPinManager() {
        return pinManager;
    }

    public EmailManager getEmailManager() {
        return emailManager;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public Set<UUID> getAuthenticatedPlayers() {
        return authenticatedPlayers;
    }

    public Set<UUID> getPinVerifiedPlayers() {
        return pinVerifiedPlayers;
    }

    public Map<UUID, Integer> getLoginAttempts() {
        return loginAttempts;
    }

    public Map<UUID, Long> getLockedPlayers() {
        return lockedPlayers;
    }

    public Set<String> getAdminList() {
        return adminList;
    }

    public boolean isAuthenticated(Player player) {
        return authenticatedPlayers.contains(player.getUniqueId());
    }

    public boolean isPinVerified(Player player) {
        return pinVerifiedPlayers.contains(player.getUniqueId());
    }

    public boolean isFullyAuthenticated(Player player) {
        UUID uuid = player.getUniqueId();
        boolean hasPin = databaseManager.hasPin(uuid);
        if (hasPin) {
            return authenticatedPlayers.contains(uuid) && pinVerifiedPlayers.contains(uuid);
        }
        return authenticatedPlayers.contains(uuid);
    }

    public void authenticatePlayer(Player player) {
        authenticatedPlayers.add(player.getUniqueId());
        loginAttempts.remove(player.getUniqueId());
    }

    public void verifyPin(Player player) {
        pinVerifiedPlayers.add(player.getUniqueId());
    }

    public void deauthenticatePlayer(Player player) {
        authenticatedPlayers.remove(player.getUniqueId());
        pinVerifiedPlayers.remove(player.getUniqueId());
    }

    public boolean isLocked(Player player) {
        UUID uuid = player.getUniqueId();
        if (!lockedPlayers.containsKey(uuid)) {
            return false;
        }
        long lockTime = lockedPlayers.get(uuid);
        int lockDuration = configManager.getConfig().getInt("security.lock-duration", 300) * 1000;
        if (System.currentTimeMillis() - lockTime >= lockDuration) {
            lockedPlayers.remove(uuid);
            loginAttempts.remove(uuid);
            return false;
        }
        return true;
    }

    public void incrementLoginAttempt(Player player) {
        UUID uuid = player.getUniqueId();
        int attempts = loginAttempts.getOrDefault(uuid, 0) + 1;
        loginAttempts.put(uuid, attempts);
        int maxAttempts = configManager.getConfig().getInt("security.max-login-attempts", 5);
        if (attempts >= maxAttempts) {
            lockedPlayers.put(uuid, System.currentTimeMillis());
        }
    }

    public int getRemainingAttempts(Player player) {
        int maxAttempts = configManager.getConfig().getInt("security.max-login-attempts", 5);
        int currentAttempts = loginAttempts.getOrDefault(player.getUniqueId(), 0);
        return maxAttempts - currentAttempts;
    }

    public long getRemainingLockTime(Player player) {
        UUID uuid = player.getUniqueId();
        if (!lockedPlayers.containsKey(uuid)) {
            return 0;
        }
        long lockTime = lockedPlayers.get(uuid);
        int lockDuration = configManager.getConfig().getInt("security.lock-duration", 300) * 1000;
        return Math.max(0, (lockDuration - (System.currentTimeMillis() - lockTime)) / 1000);
    }

    public boolean isPinLocked(Player player) {
        UUID uuid = player.getUniqueId();
        if (!pinLockedPlayers.containsKey(uuid)) {
            return false;
        }
        long lockTime = pinLockedPlayers.get(uuid);
        int lockDuration = configManager.getConfig().getInt("security.pin-lock-duration", 300) * 1000;
        if (System.currentTimeMillis() - lockTime >= lockDuration) {
            pinLockedPlayers.remove(uuid);
            pinAttempts.remove(uuid);
            return false;
        }
        return true;
    }

    public void incrementPinAttempt(Player player) {
        UUID uuid = player.getUniqueId();
        int attempts = pinAttempts.getOrDefault(uuid, 0) + 1;
        pinAttempts.put(uuid, attempts);
        int maxAttempts = configManager.getConfig().getInt("security.max-pin-attempts", 3);
        if (attempts >= maxAttempts) {
            pinLockedPlayers.put(uuid, System.currentTimeMillis());
        }
    }

    public int getRemainingPinAttempts(Player player) {
        int maxAttempts = configManager.getConfig().getInt("security.max-pin-attempts", 3);
        int currentAttempts = pinAttempts.getOrDefault(player.getUniqueId(), 0);
        return maxAttempts - currentAttempts;
    }

    public long getRemainingPinLockTime(Player player) {
        UUID uuid = player.getUniqueId();
        if (!pinLockedPlayers.containsKey(uuid)) {
            return 0;
        }
        long lockTime = pinLockedPlayers.get(uuid);
        int lockDuration = configManager.getConfig().getInt("security.pin-lock-duration", 300) * 1000;
        return Math.max(0, (lockDuration - (System.currentTimeMillis() - lockTime)) / 1000);
    }

    public void clearPinAttempts(Player player) {
        pinAttempts.remove(player.getUniqueId());
    }

    public boolean isIpLocked(String ip) {
        if (!ipLockedList.containsKey(ip)) {
            return false;
        }
        long lockTime = ipLockedList.get(ip);
        int lockDuration = configManager.getConfig().getInt("security.ip-lock-duration", 600) * 1000;
        if (System.currentTimeMillis() - lockTime >= lockDuration) {
            ipLockedList.remove(ip);
            ipLoginAttempts.remove(ip);
            return false;
        }
        return true;
    }

    public void incrementIpLoginAttempt(String ip) {
        int attempts = ipLoginAttempts.getOrDefault(ip, 0) + 1;
        ipLoginAttempts.put(ip, attempts);
        int maxAttempts = configManager.getConfig().getInt("security.max-ip-login-attempts", 10);
        if (attempts >= maxAttempts) {
            ipLockedList.put(ip, System.currentTimeMillis());
        }
    }

    public int getRemainingIpAttempts(String ip) {
        int maxAttempts = configManager.getConfig().getInt("security.max-ip-login-attempts", 10);
        int currentAttempts = ipLoginAttempts.getOrDefault(ip, 0);
        return maxAttempts - currentAttempts;
    }

    public long getRemainingIpLockTime(String ip) {
        if (!ipLockedList.containsKey(ip)) {
            return 0;
        }
        long lockTime = ipLockedList.get(ip);
        int lockDuration = configManager.getConfig().getInt("security.ip-lock-duration", 600) * 1000;
        return Math.max(0, (lockDuration - (System.currentTimeMillis() - lockTime)) / 1000);
    }

    public void clearIpLoginAttempts(String ip) {
        ipLoginAttempts.remove(ip);
    }

    public boolean isIpPinLocked(String ip) {
        if (!ipPinLockedList.containsKey(ip)) {
            return false;
        }
        long lockTime = ipPinLockedList.get(ip);
        int lockDuration = configManager.getConfig().getInt("security.ip-pin-lock-duration", 600) * 1000;
        if (System.currentTimeMillis() - lockTime >= lockDuration) {
            ipPinLockedList.remove(ip);
            ipPinAttempts.remove(ip);
            return false;
        }
        return true;
    }

    public void incrementIpPinAttempt(String ip) {
        int attempts = ipPinAttempts.getOrDefault(ip, 0) + 1;
        ipPinAttempts.put(ip, attempts);
        int maxAttempts = configManager.getConfig().getInt("security.max-ip-pin-attempts", 6);
        if (attempts >= maxAttempts) {
            ipPinLockedList.put(ip, System.currentTimeMillis());
        }
    }

    public int getRemainingIpPinAttempts(String ip) {
        int maxAttempts = configManager.getConfig().getInt("security.max-ip-pin-attempts", 6);
        int currentAttempts = ipPinAttempts.getOrDefault(ip, 0);
        return maxAttempts - currentAttempts;
    }

    public long getRemainingIpPinLockTime(String ip) {
        if (!ipPinLockedList.containsKey(ip)) {
            return 0;
        }
        long lockTime = ipPinLockedList.get(ip);
        int lockDuration = configManager.getConfig().getInt("security.ip-pin-lock-duration", 600) * 1000;
        return Math.max(0, (lockDuration - (System.currentTimeMillis() - lockTime)) / 1000);
    }

    public void clearIpPinAttempts(String ip) {
        ipPinAttempts.remove(ip);
    }

    public void forceIpLock(String ip) {
        ipLockedList.put(ip, System.currentTimeMillis());
    }

    public void forceIpPinLock(String ip) {
        ipPinLockedList.put(ip, System.currentTimeMillis());
    }
}
