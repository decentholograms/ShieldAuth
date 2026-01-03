package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public LoginCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String ip = player.getAddress().getAddress().getHostAddress();

        if (plugin.isAuthenticated(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-already"));
            return true;
        }

        if (!plugin.getDatabaseManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-not-registered"));
            return true;
        }

        if (plugin.isIpLocked(ip)) {
            long remaining = plugin.getRemainingIpLockTime(ip);
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.ip-locked", "{time}", String.valueOf(remaining)));
            return true;
        }

        if (plugin.isLocked(player)) {
            long remaining = plugin.getRemainingLockTime(player);
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-locked", "{time}", String.valueOf(remaining)));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-usage"));
            return true;
        }

        String password = args[0];

        if (plugin.getDatabaseManager().login(player.getUniqueId(), password)) {
            plugin.authenticatePlayer(player);
            plugin.clearIpLoginAttempts(ip);
            plugin.getDatabaseManager().updateLastLogin(player.getUniqueId(), ip);
            plugin.getDatabaseManager().saveSession(player.getUniqueId(), ip);

            if (plugin.getDatabaseManager().hasPin(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-success-pin-required"));
                plugin.getPinManager().openPinGui(player);
            } else {
                plugin.verifyPin(player);
                player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-success"));
                sendSuccessTitle(player);

                if (plugin.getConfigManager().getConfig().getBoolean("discord.enabled", false)) {
                    plugin.getDiscordWebhook().sendLoginNotification(player.getName(), ip, false);
                }
            }
        } else {
            plugin.incrementLoginAttempt(player);
            plugin.incrementIpLoginAttempt(ip);
            
            int remainingPlayer = plugin.getRemainingAttempts(player);
            int remainingIp = plugin.getRemainingIpAttempts(ip);
            int remaining = Math.min(remainingPlayer, remainingIp);
            
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.login-wrong-password", "{attempts}", String.valueOf(remaining)));

            if (remainingIp <= 0 || remainingPlayer <= 0) {
                plugin.forceIpLock(ip);
                long lockTime = plugin.getRemainingIpLockTime(ip);
                boolean kickEnabled = plugin.getConfigManager().getConfig().getBoolean("security.ip-block-kick-enabled", true);
                if (kickEnabled) {
                    String kickMessage = plugin.getConfigManager().getColoredMessage("messages.block-ip-locked", "{time}", formatTime(lockTime));
                    player.kickPlayer(kickMessage);
                } else {
                    player.sendMessage(plugin.getConfigManager().getColoredMessage("messages.ip-locked", "{time}", String.valueOf(lockTime)));
                }
            }
        }

        return true;
    }
    
    private String formatTime(long seconds) {
        if (seconds >= 3600) {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            return hours + "h " + mins + "m";
        } else if (seconds >= 60) {
            long mins = seconds / 60;
            long secs = seconds % 60;
            return mins + "m " + secs + "s";
        } else {
            return seconds + "s";
        }
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
