package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VerifyEmailCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public VerifyEmailCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.isFullyAuthenticated(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.not-authenticated"));
            return true;
        }

        if (plugin.getDatabaseManager().isEmailVerified(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-already-verified"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-verify-usage"));
            return true;
        }

        String code = args[0];

        if (plugin.getDatabaseManager().verifyEmail(player.getUniqueId(), code)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-verify-success"));
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-verify-wrong-code"));
        }

        return true;
    }
}
