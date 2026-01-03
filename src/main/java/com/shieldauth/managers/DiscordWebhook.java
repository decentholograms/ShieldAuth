package com.shieldauth.managers;

import com.google.gson.JsonObject;
import com.shieldauth.ShieldAuth;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhook {

    private final ShieldAuth plugin;

    public DiscordWebhook(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    public void sendLoginNotification(String playerName, String ip, boolean isRegister) {
        if (!plugin.getConfigManager().getConfig().getBoolean("discord.enabled", false)) {
            return;
        }

        String webhookUrl = plugin.getConfigManager().getConfig().getString("discord.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                int red = plugin.getConfigManager().getConfig().getInt("discord.embed-color-red", 255);
                int green = plugin.getConfigManager().getConfig().getInt("discord.embed-color-green", 0);
                int blue = plugin.getConfigManager().getConfig().getInt("discord.embed-color-blue", 0);
                int color = (red << 16) + (green << 8) + blue;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());

                JsonObject embed = new JsonObject();
                embed.addProperty("title", isRegister ? "New Registration" : "Player Login");
                embed.addProperty("color", color);

                JsonObject field1 = new JsonObject();
                field1.addProperty("name", "Player");
                field1.addProperty("value", playerName);
                field1.addProperty("inline", true);

                JsonObject field2 = new JsonObject();
                field2.addProperty("name", "IP Address");
                field2.addProperty("value", "||" + ip + "||");
                field2.addProperty("inline", true);

                JsonObject field3 = new JsonObject();
                field3.addProperty("name", "Time");
                field3.addProperty("value", timestamp);
                field3.addProperty("inline", false);

                com.google.gson.JsonArray fields = new com.google.gson.JsonArray();
                fields.add(field1);
                fields.add(field2);
                fields.add(field3);

                embed.add("fields", fields);

                JsonObject footer = new JsonObject();
                footer.addProperty("text", "ShieldAuth Security System");
                embed.add("footer", footer);

                com.google.gson.JsonArray embeds = new com.google.gson.JsonArray();
                embeds.add(embed);

                JsonObject payload = new JsonObject();
                payload.addProperty("username", "ShieldAuth");
                payload.add("embeds", embeds);

                sendWebhook(webhookUrl, payload.toString());

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    public void sendSecurityAlert(String playerName, String message, String ip) {
        if (!plugin.getConfigManager().getConfig().getBoolean("discord.enabled", false)) {
            return;
        }

        String webhookUrl = plugin.getConfigManager().getConfig().getString("discord.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                int color = (255 << 16) + (0 << 8) + 0;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());

                JsonObject embed = new JsonObject();
                embed.addProperty("title", "Security Alert");
                embed.addProperty("description", message);
                embed.addProperty("color", color);

                JsonObject field1 = new JsonObject();
                field1.addProperty("name", "Player");
                field1.addProperty("value", playerName);
                field1.addProperty("inline", true);

                JsonObject field2 = new JsonObject();
                field2.addProperty("name", "IP Address");
                field2.addProperty("value", "||" + ip + "||");
                field2.addProperty("inline", true);

                JsonObject field3 = new JsonObject();
                field3.addProperty("name", "Time");
                field3.addProperty("value", timestamp);
                field3.addProperty("inline", false);

                com.google.gson.JsonArray fields = new com.google.gson.JsonArray();
                fields.add(field1);
                fields.add(field2);
                fields.add(field3);

                embed.add("fields", fields);

                JsonObject footer = new JsonObject();
                footer.addProperty("text", "ShieldAuth Security System");
                embed.add("footer", footer);

                com.google.gson.JsonArray embeds = new com.google.gson.JsonArray();
                embeds.add(embed);

                JsonObject payload = new JsonObject();
                payload.addProperty("username", "ShieldAuth");
                payload.add("embeds", embeds);

                sendWebhook(webhookUrl, payload.toString());

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    private void sendWebhook(String webhookUrl, String payload) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "ShieldAuth");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                plugin.getLogger().warning("Discord webhook returned code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send webhook: " + e.getMessage());
        }
    }
}
