package com.shieldauth.listeners;

import com.shieldauth.ShieldAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final ShieldAuth plugin;

    public PlayerChatListener(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.isFullyAuthenticated(player)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.action-blocked"));
    }
}
