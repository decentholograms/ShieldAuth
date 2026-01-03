package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemovePinCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public RemovePinCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String permission = plugin.getConfigManager().getConfig().getString("permissions.user-pin", "shieldauth.pin");

        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-no-permission"));
            return true;
        }

        if (!plugin.isFullyAuthenticated(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.not-authenticated"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-remove-usage"));
            return true;
        }

        String pin = args[0];

        if (!plugin.getDatabaseManager().verifyPin(player.getUniqueId(), pin)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-wrong"));
            return true;
        }

        if (plugin.getDatabaseManager().removePin(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-removed-success"));
        }

        return true;
    }
}
