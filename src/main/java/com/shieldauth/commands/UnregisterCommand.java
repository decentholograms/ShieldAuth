package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnregisterCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public UnregisterCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String permission = plugin.getConfigManager().getConfig().getString("permissions.user-unregister", "shieldauth.unregister");

        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.unregister-no-permission"));
            return true;
        }

        if (!plugin.getDatabaseManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.unregister-not-registered"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.unregister-usage"));
            return true;
        }

        String password = args[0];

        if (!plugin.getDatabaseManager().login(player.getUniqueId(), password)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.unregister-wrong-password"));
            return true;
        }

        if (plugin.getDatabaseManager().unregister(player.getUniqueId())) {
            plugin.deauthenticatePlayer(player);
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.unregister-success"));
        }

        return true;
    }
}
