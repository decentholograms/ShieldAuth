package com.shieldauth.listeners;

import com.shieldauth.ShieldAuth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerPreLoginListener implements Listener {

    private final ShieldAuth plugin;

    public PlayerPreLoginListener(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String ip = event.getAddress().getHostAddress();
        boolean kickEnabled = plugin.getConfigManager().getConfig().getBoolean("security.ip-block-kick-enabled", true);
        
        if (!kickEnabled) {
            return;
        }
        
        if (plugin.isIpLocked(ip)) {
            long remaining = plugin.getRemainingIpLockTime(ip);
            String message = plugin.getConfigManager().getColoredMessage("messages.block-ip-login-denied", "{time}", formatTime(remaining));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
            return;
        }
        
        if (plugin.isIpPinLocked(ip)) {
            long remaining = plugin.getRemainingIpPinLockTime(ip);
            String message = plugin.getConfigManager().getColoredMessage("messages.block-ip-pin-denied", "{time}", formatTime(remaining));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
            return;
        }
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
}
