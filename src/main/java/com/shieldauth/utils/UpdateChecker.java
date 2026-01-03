package com.shieldauth.utils;

import com.shieldauth.ShieldAuth;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final ShieldAuth plugin;
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable = false;

    public UpdateChecker(ShieldAuth plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public void checkForUpdates() {
        if (!plugin.getConfigManager().getConfig().getBoolean("update-checker.enabled", true)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String apiUrl = plugin.getConfigManager().getConfig().getString("update-checker.github-api-url", "");
                if (apiUrl.isEmpty()) {
                    return;
                }

                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("User-Agent", "ShieldAuth-UpdateChecker");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String json = response.toString();
                    int tagIndex = json.indexOf("\"tag_name\"");
                    if (tagIndex != -1) {
                        int startQuote = json.indexOf("\"", tagIndex + 11);
                        int endQuote = json.indexOf("\"", startQuote + 1);
                        latestVersion = json.substring(startQuote + 1, endQuote).replace("v", "");

                        if (!currentVersion.equals(latestVersion) && isNewerVersion(latestVersion, currentVersion)) {
                            updateAvailable = true;
                            String githubUrl = plugin.getConfigManager().getConfig().getString("update-checker.github-url", "");
                            
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                String outdatedMsg = plugin.getConfigManager().getColoredMessage("messages.update-available")
                                        .replace("{current}", currentVersion)
                                        .replace("{latest}", latestVersion)
                                        .replace("{url}", githubUrl);
                                Bukkit.getConsoleSender().sendMessage(outdatedMsg);
                            });
                        }
                    }
                }
                connection.disconnect();
            } catch (Exception ignored) {
            }
        });
    }

    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }
        return false;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
