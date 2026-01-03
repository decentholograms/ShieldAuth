package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPinCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public SetPinCommand(ShieldAuth plugin) {
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
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-set-usage"));
            return true;
        }

        String pin = args[0];
        int pinLength = plugin.getConfigManager().getConfig().getInt("security.pin-length", 4);

        if (pin.length() != pinLength) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-invalid-length", "{length}", String.valueOf(pinLength)));
            return true;
        }

        if (!pin.matches("\\d+")) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-invalid-format"));
            return true;
        }

        if (plugin.getDatabaseManager().setPin(player.getUniqueId(), pin)) {
            plugin.verifyPin(player);
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-set-success"));
        }

        return true;
    }
}
