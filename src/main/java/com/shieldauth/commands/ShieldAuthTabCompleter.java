package com.shieldauth.commands;

import com.shieldauth.ShieldAuth;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShieldAuthTabCompleter implements TabCompleter {

    private final ShieldAuth plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "reload", "addadmin", "removeadmin", "list", "info", "forcelogin", "forceunregister", "forcesetpin", "forceremovepin", "help"
    );

    public ShieldAuthTabCompleter(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String adminPermission = plugin.getConfigManager().getConfig().getString("permissions.admin", "shieldauth.admin");

        if (!sender.hasPermission(adminPermission) && !isAdmin(sender)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "addadmin":
                case "info":
                case "forcelogin":
                case "forceunregister":
                case "forcesetpin":
                case "forceremovepin":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "removeadmin":
                    return plugin.getAdminList().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    private boolean isAdmin(CommandSender sender) {
        if (sender instanceof Player) {
            return plugin.getAdminList().contains(((Player) sender).getName().toLowerCase());
        }
        return true;
    }
}
