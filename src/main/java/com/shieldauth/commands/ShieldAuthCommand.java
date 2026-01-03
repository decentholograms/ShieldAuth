package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import com.shieldauth.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShieldAuthCommand implements CommandExecutor {

    private final ShieldAuth plugin;

    public ShieldAuthCommand(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String adminPermission = plugin.getConfigManager().getConfig().getString("permissions.admin", "shieldauth.admin");

        if (!sender.hasPermission(adminPermission) && !isAdmin(sender)) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "addadmin":
                handleAddAdmin(sender, args);
                break;
            case "removeadmin":
                handleRemoveAdmin(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "forcelogin":
                handleForceLogin(sender, args);
                break;
            case "forceunregister":
                handleForceUnregister(sender, args);
                break;
            case "forcesetpin":
                handleForceSetPin(sender, args);
                break;
            case "forceremovepin":
                handleForceRemovePin(sender, args);
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private boolean isAdmin(CommandSender sender) {
        if (sender instanceof Player) {
            return plugin.getAdminList().contains(((Player) sender).getName().toLowerCase());
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadPlugin();
        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-reload-success"));
    }

    private void handleAddAdmin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-addadmin-usage"));
            return;
        }

        String playerName = args[1].toLowerCase();

        if (plugin.getAdminList().contains(playerName)) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-addadmin-already", "{player}", args[1]));
            return;
        }

        plugin.getAdminList().add(playerName);
        plugin.saveAdminList();
        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-addadmin-success", "{player}", args[1]));
    }

    private void handleRemoveAdmin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-removeadmin-usage"));
            return;
        }

        String playerName = args[1].toLowerCase();

        if (!plugin.getAdminList().contains(playerName)) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-removeadmin-not-found", "{player}", args[1]));
            return;
        }

        plugin.getAdminList().remove(playerName);
        plugin.saveAdminList();
        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-removeadmin-success", "{player}", args[1]));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-list-header"));

        if (plugin.getAdminList().isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-list-empty"));
            return;
        }

        for (String admin : plugin.getAdminList()) {
            sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-list-entry", "{player}", admin));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-info-header", "{player}", "Unknown"));
            return;
        }

        String playerName = args[1];
        DatabaseManager.PlayerData data = plugin.getDatabaseManager().getPlayerDataByName(playerName);

        if (data == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-not-found", "{player}", playerName));
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-info-header", "{player}", data.username));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-info-registered", "{status}", "Yes"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-info-has-pin", "{status}", data.hasPin ? "Yes" : "No"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-info-has-email", "{status}", data.email != null ? (data.emailVerified ? "Verified" : "Unverified") : "No"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-info-last-login", "{date}", data.lastLogin > 0 ? sdf.format(new Date(data.lastLogin)) : "Never"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-info-last-ip", "{ip}", data.lastIp != null ? data.lastIp : "Unknown"));
    }

    private void handleForceLogin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forcelogin-not-online", "{player}", "Unknown"));
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forcelogin-not-online", "{player}", playerName));
            return;
        }

        plugin.authenticatePlayer(target);
        if (plugin.getDatabaseManager().hasPin(target.getUniqueId())) {
            plugin.verifyPin(target);
        }

        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forcelogin-success", "{player}", playerName));
    }

    private void handleForceUnregister(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-not-found", "{player}", "Unknown"));
            return;
        }

        String playerName = args[1];
        DatabaseManager.PlayerData data = plugin.getDatabaseManager().getPlayerDataByName(playerName);

        if (data == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-not-found", "{player}", playerName));
            return;
        }

        plugin.getDatabaseManager().unregister(data.uuid);

        Player target = Bukkit.getPlayer(data.uuid);
        if (target != null) {
            plugin.deauthenticatePlayer(target);
        }

        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-success", "{player}", playerName));
    }

    private void handleForceSetPin(CommandSender sender, String[] args) {
        String permission = plugin.getConfigManager().getConfig().getString("permissions.forcesetpin", "shieldauth.forcesetpin");
        if (!sender.hasPermission(permission) && !isAdmin(sender)) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-no-permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forcesetpin-usage"));
            return;
        }

        String playerName = args[1];
        String pin = args[2];
        int pinLength = plugin.getConfigManager().getConfig().getInt("security.pin-length", 4);

        if (pin.length() != pinLength || !pin.matches("\\d+")) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-invalid-length", "{length}", String.valueOf(pinLength)));
            return;
        }

        DatabaseManager.PlayerData data = plugin.getDatabaseManager().getPlayerDataByName(playerName);
        if (data == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-not-found", "{player}", playerName));
            return;
        }

        plugin.getDatabaseManager().setPin(data.uuid, pin);
        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forcesetpin-success", "{player}", playerName));
    }

    private void handleForceRemovePin(CommandSender sender, String[] args) {
        String permission = plugin.getConfigManager().getConfig().getString("permissions.forceremovepin", "shieldauth.forceremovepin");
        if (!sender.hasPermission(permission) && !isAdmin(sender)) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-not-found", "{player}", "Unknown"));
            return;
        }

        String playerName = args[1];
        DatabaseManager.PlayerData data = plugin.getDatabaseManager().getPlayerDataByName(playerName);

        if (data == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceunregister-not-found", "{player}", playerName));
            return;
        }

        if (!data.hasPin) {
            sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceremovepin-no-pin", "{player}", playerName));
            return;
        }

        plugin.getDatabaseManager().removePin(data.uuid);

        Player target = Bukkit.getPlayer(data.uuid);
        if (target != null && plugin.getPinManager().hasOpenGui(target)) {
            target.closeInventory();
            plugin.getPinManager().removePlayer(target);
            plugin.verifyPin(target);
        }

        sender.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.admin-forceremovepin-success", "{player}", playerName));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-header"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-reload"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-addadmin"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-removeadmin"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-list"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-info"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-forcelogin"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-forceunregister"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-forcesetpin"));
        sender.sendMessage(plugin.getConfigManager().getColoredMessage("messages.admin-help-forceremovepin"));
    }
}
