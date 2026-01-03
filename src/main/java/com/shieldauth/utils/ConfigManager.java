package com.shieldauth.utils;

import com.shieldauth.ShieldAuth;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final ShieldAuth plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String path) {
        String message = config.getString(path);
        if (message == null) {
            return "Message not found: " + path;
        }
        return message;
    }

    public String getColoredMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getMessage(path));
    }

    public String getColoredMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return getColoredMessage("messages.prefix");
    }

    public String getPrefixedMessage(String path) {
        return getPrefix() + getColoredMessage(path);
    }

    public String getPrefixedMessage(String path, String... replacements) {
        return getPrefix() + getColoredMessage(path, replacements);
    }
}
