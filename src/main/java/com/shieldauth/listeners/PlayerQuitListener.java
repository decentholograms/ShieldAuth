package com.shieldauth.listeners;

import com.shieldauth.ShieldAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final ShieldAuth plugin;

    public PlayerQuitListener(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getPinManager().removePlayer(player);
        plugin.getLoginAttempts().remove(player.getUniqueId());
        plugin.getLockedPlayers().remove(player.getUniqueId());

        if (plugin.isFullyAuthenticated(player)) {
            String ip = player.getAddress().getAddress().getHostAddress();
            plugin.getSessionManager().saveSession(player.getUniqueId(), ip);
        }

        plugin.deauthenticatePlayer(player);
    }
}
