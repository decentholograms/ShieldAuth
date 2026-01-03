package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import com.shieldauth.utils.EncryptionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetEmailCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public SetEmailCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String permission = plugin.getConfigManager().getConfig().getString("permissions.user-email", "shieldauth.email");

        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-no-permission"));
            return true;
        }

        if (!plugin.isFullyAuthenticated(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.not-authenticated"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-set-usage"));
            return true;
        }

        String email = args[0];

        if (!isValidEmail(email)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-invalid-format"));
            return true;
        }

        String verificationCode = EncryptionUtil.generateRandomCode(6);

        if (plugin.getDatabaseManager().setEmail(player.getUniqueId(), email, verificationCode)) {
            if (plugin.getConfigManager().getConfig().getBoolean("email.enabled", false)) {
                plugin.getEmailManager().sendVerificationEmail(email, verificationCode, player.getName());
            }
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.email-set-success"));
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
