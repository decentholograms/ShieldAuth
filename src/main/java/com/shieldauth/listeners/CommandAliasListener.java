package com.shieldauth.listeners;

import com.shieldauth.ShieldAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandAliasListener implements Listener {

    private final ShieldAuth plugin;
    private final Map<String, String> aliasMap = new HashMap<>();

    public CommandAliasListener(ShieldAuth plugin) {
        this.plugin = plugin;
        loadAliases();
    }

    public void loadAliases() {
        aliasMap.clear();
        
        Map<String, List<String>> aliasConfig = new HashMap<>();
        
        if (plugin.getConfigManager().getConfig().contains("aliases")) {
            for (String command : plugin.getConfigManager().getConfig().getConfigurationSection("aliases").getKeys(false)) {
                List<String> aliases = plugin.getConfigManager().getConfig().getStringList("aliases." + command);
                for (String alias : aliases) {
                    aliasMap.put(alias.toLowerCase(), command.toLowerCase());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String message = event.getMessage();
        if (!message.startsWith("/")) {
            return;
        }

        String[] parts = message.substring(1).split(" ", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? " " + parts[1] : "";

        if (aliasMap.containsKey(commandName)) {
            String realCommand = aliasMap.get(commandName);
            event.setMessage("/" + realCommand + args);
        }
    }

    public void reload() {
        loadAliases();
    }
}
