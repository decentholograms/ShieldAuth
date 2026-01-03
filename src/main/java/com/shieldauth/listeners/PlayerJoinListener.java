package com.shieldauth.listeners;

import com.shieldauth.ShieldAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final ShieldAuth plugin;

    public PlayerJoinListener(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress().getAddress().getHostAddress();

        boolean isRegistered = plugin.getDatabaseManager().isRegistered(player.getUniqueId());
        boolean hasPin = plugin.getDatabaseManager().hasPin(player.getUniqueId());
        boolean hasValidSession = plugin.getSessionManager().hasValidSession(player.getUniqueId(), ip);
        
        final long joinTime = System.currentTimeMillis();
        final boolean timeoutEnabled = plugin.getConfigManager().getConfig().getBoolean("security.auth-timeout-enabled", true);
        final int registerTimeout = plugin.getConfigManager().getConfig().getInt("security.register-timeout", 60);
        final int loginTimeout = plugin.getConfigManager().getConfig().getInt("security.login-timeout", 60);
        final int pinTimeout = plugin.getConfigManager().getConfig().getInt("security.pin-timeout", 30);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                
                if (plugin.isIpLocked(ip)) {
                    long remaining = plugin.getRemainingIpLockTime(ip);
                    player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.ip-locked-join", "{time}", String.valueOf(remaining)));
                }
                if (plugin.isIpPinLocked(ip)) {
                    long remaining = plugin.getRemainingIpPinLockTime(ip);
                    player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.ip-pin-locked-join", "{time}", String.valueOf(remaining)));
                }
                if (plugin.isLocked(player)) {
                    long remaining = plugin.getRemainingLockTime(player);
                    player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.account-locked-join", "{time}", String.valueOf(remaining)));
                }
                if (plugin.isPinLocked(player)) {
                    long remaining = plugin.getRemainingPinLockTime(player);
                    player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-locked-join", "{time}", String.valueOf(remaining)));
                }
            }
        }.runTaskLater(plugin, 10L);

        boolean sessionEnabled = plugin.getConfigManager().getConfig().getBoolean("security.session-enabled", false);
        
        if (sessionEnabled && isRegistered && hasValidSession && !hasPin) {
            plugin.authenticatePlayer(player);
            plugin.verifyPin(player);
            plugin.getDatabaseManager().updateLastLogin(player.getUniqueId(), ip);
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.session-restored"));
            sendSuccessTitle(player);
            return;
        }
        
        if (sessionEnabled && isRegistered && hasValidSession && hasPin) {
            plugin.authenticatePlayer(player);
            plugin.getDatabaseManager().updateLastLogin(player.getUniqueId(), ip);
        }

        if (timeoutEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        return;
                    }

                    if (plugin.isFullyAuthenticated(player)) {
                        cancel();
                        return;
                    }

                    long elapsed = (System.currentTimeMillis() - joinTime) / 1000;
                    boolean currentlyRegistered = plugin.getDatabaseManager().isRegistered(player.getUniqueId());
                    boolean currentHasPin = plugin.getDatabaseManager().hasPin(player.getUniqueId());

                    if (!currentlyRegistered && elapsed >= registerTimeout) {
                        String kickMsg = plugin.getConfigManager().getColoredMessage("messages.kick-register-timeout", "{time}", formatTime(registerTimeout));
                        player.kickPlayer(kickMsg);
                        cancel();
                        return;
                    }

                    if (currentlyRegistered && !plugin.isAuthenticated(player) && elapsed >= loginTimeout) {
                        String kickMsg = plugin.getConfigManager().getColoredMessage("messages.kick-login-timeout", "{time}", formatTime(loginTimeout));
                        player.kickPlayer(kickMsg);
                        cancel();
                        return;
                    }

                    if (plugin.isAuthenticated(player) && currentHasPin && !plugin.isPinVerified(player)) {
                        long pinWaitTime = elapsed - loginTimeout;
                        if (pinWaitTime < 0) pinWaitTime = elapsed;
                        
                        if (elapsed >= (loginTimeout + pinTimeout)) {
                            String kickMsg = plugin.getConfigManager().getColoredMessage("messages.kick-pin-timeout", "{time}", formatTime(pinTimeout));
                            player.kickPlayer(kickMsg);
                            cancel();
                            return;
                        }
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (plugin.isFullyAuthenticated(player)) {
                    cancel();
                    return;
                }

                ticks++;

                if (!isRegistered) {
                    sendRegisterTitle(player);
                } else if (!plugin.isAuthenticated(player)) {
                    sendLoginTitle(player);
                } else if (hasPin && !plugin.isPinVerified(player)) {
                    sendPinTitle(player);
                    if (!plugin.getPinManager().hasOpenGui(player)) {
                        plugin.getPinManager().openPinGui(player);
                    }
                }

                if (ticks >= 6000) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 60L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                
                if (!isRegistered) {
                    sendRegisterTitle(player);
                } else if (!plugin.isAuthenticated(player)) {
                    sendLoginTitle(player);
                } else if (hasPin && !plugin.isPinVerified(player)) {
                    sendPinTitle(player);
                    plugin.getPinManager().openPinGui(player);
                }
            }
        }.runTaskLater(plugin, 5L);
    }

    private void sendRegisterTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.register.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.register.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.register.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.register.stay", 70);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.register.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void sendLoginTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.login.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.login.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.login.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.login.stay", 70);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.login.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void sendPinTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.pin.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.pin.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.pin.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.pin.stay", 70);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.pin.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    private void sendSuccessTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.success.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.success.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.success.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.success.stay", 40);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.success.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
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
