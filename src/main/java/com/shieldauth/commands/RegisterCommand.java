package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public RegisterCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getDatabaseManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-already"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-usage"));
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        int minLength = plugin.getConfigManager().getConfig().getInt("security.password-min-length", 6);
        int maxLength = plugin.getConfigManager().getConfig().getInt("security.password-max-length", 32);

        if (password.length() < minLength) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-password-too-short", "{min}", String.valueOf(minLength)));
            return true;
        }

        if (password.length() > maxLength) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-password-too-long", "{max}", String.valueOf(maxLength)));
            return true;
        }

        if (!password.equals(confirmPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-password-mismatch"));
            return true;
        }

        String ip = player.getAddress().getAddress().getHostAddress();

        if (plugin.getDatabaseManager().register(player.getUniqueId(), player.getName(), password, ip)) {
            plugin.authenticatePlayer(player);
            plugin.getDatabaseManager().saveSession(player.getUniqueId(), ip);
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-success"));
            sendSuccessTitle(player);

            if (plugin.getConfigManager().getConfig().getBoolean("discord.enabled", false)) {
                plugin.getDiscordWebhook().sendLoginNotification(player.getName(), ip, true);
            }
        }

        return true;
    }

    private void sendSuccessTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.success.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.success.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.success.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.success.stay", 40);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.success.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
