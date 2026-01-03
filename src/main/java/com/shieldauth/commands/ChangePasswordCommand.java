package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public ChangePasswordCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String permission = plugin.getConfigManager().getConfig().getString("permissions.user-changepassword", "shieldauth.changepassword");

        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.changepassword-no-permission"));
            return true;
        }

        if (!plugin.isFullyAuthenticated(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.not-authenticated"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.changepassword-usage"));
            return true;
        }

        String currentPassword = args[0];
        String newPassword = args[1];

        if (currentPassword.equals(newPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.changepassword-same-password"));
            return true;
        }

        int minLength = plugin.getConfigManager().getConfig().getInt("security.password-min-length", 6);
        int maxLength = plugin.getConfigManager().getConfig().getInt("security.password-max-length", 32);

        if (newPassword.length() < minLength) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-password-too-short", "{min}", String.valueOf(minLength)));
            return true;
        }

        if (newPassword.length() > maxLength) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.register-password-too-long", "{max}", String.valueOf(maxLength)));
            return true;
        }

        if (plugin.getDatabaseManager().changePassword(player.getUniqueId(), currentPassword, newPassword)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.changepassword-success"));
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.changepassword-wrong-password"));
        }

        return true;
    }
}
