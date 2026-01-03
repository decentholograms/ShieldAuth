package com.shieldauth.listeners;

import com.shieldauth.ShieldAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PlayerCommandListener implements Listener {

    private final ShieldAuth plugin;

    public PlayerCommandListener(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (plugin.isFullyAuthenticated(player)) {
            return;
        }

        String command = event.getMessage().toLowerCase().split(" ")[0];
        List<String> allowedCommands = plugin.getConfigManager().getConfig().getStringList("security.allowed-commands");

        for (String allowed : allowedCommands) {
            if (command.equalsIgnoreCase(allowed) || command.equals(allowed.toLowerCase())) {
                return;
            }
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.command-blocked"));
    }
}
