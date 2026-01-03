package com.shieldauth.managers;

import com.shieldauth.ShieldAuth;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PinManager {

    private final ShieldAuth plugin;
    private final Map<UUID, StringBuilder> pinInputs = new HashMap<>();
    private final Map<UUID, Inventory> openInventories = new HashMap<>();

    private static final String[] NUMBER_TEXTURES = {
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFhOTQ2M2ZkM2M0MzNkNWUxZDlmZWM2ZDVkNGIwOWE4M2E5NzBhMDk0NmU2ZTNiMTgxMjE4NTU5ZWY1ODMifX19",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhNmYwZTg0ZGVlZmM2MTU1ZjJkOGNhNTM0Njc4MWJmN2U0MDFkZTI3Y2Q5NGJiMjA3OTRkYjNkZjcifX19",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZmYWI5OTFkMDgzOTkzY2I4M2U0YmNmNDRhMGJlYjJhMzJlYzZlZGU0ZWJiMzc0YmJkZThjM2JlMjM2N2Q4In19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2JlYWIyZWFhZTgzYWI5NjRkNjQ1NDNiNjkyYmE0MWNkZTI5In19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlNzhmYjIyNDI0MjMyZGMyN2I4MWZiY2I0N2ZkMjRjMWFjZjc2MDk4NzUzZjJkOWMyODU5ODI4N2RiNSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ1N2UzYmM4OGE2NTczMGUzMWExNGUzZjQxMmI4NWE3YWIwMWJlNjE4MzJjNjM0OGNmMjFhZDMyMTQyIn19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM0YjM2ZGU3ZDY3OWI4YmJjNDk4YTJmMGQ1ZTU0N2QyYTFkMWE1ZDc3MTRmN2JlMjk4NzUzYTgzMmI3NyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNmM2M0MWQ4ZTIwZjMzZDczNDM1NjI0YWM4Y2VlY2MwMjM0NzRkY2VkOTU5ZmFkN2RlM2U4YjdjZjY2MCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFiNTJiYjhkN2VjNjRkZDZmMzE3ZjRkNjA3ZDQ1NDQ0ZTQ2Y2ViYTE3OTI1ZmU0M2Y1ZjU2MzJkMDY2NiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5M2RjMGQ0YzVlODBmZjlhOGE0NWRjYjc3ZTRmYmIwOTcxNzk1ZGY1ODM2ZjY4NjdjOGU0YTkzOGE0In19fQ=="
    };

    public PinManager(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    public void openPinGui(Player player) {
        int pinLength = plugin.getConfigManager().getConfig().getInt("security.pin-length", 4);
        String title = plugin.getConfigManager().getColoredMessage("gui.pin-title");
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        pinInputs.put(player.getUniqueId(), new StringBuilder());

        inventory.setItem(10, createNumberItem(1));
        inventory.setItem(11, createNumberItem(2));
        inventory.setItem(12, createNumberItem(3));
        inventory.setItem(19, createNumberItem(4));
        inventory.setItem(20, createNumberItem(5));
        inventory.setItem(21, createNumberItem(6));
        inventory.setItem(28, createNumberItem(7));
        inventory.setItem(29, createNumberItem(8));
        inventory.setItem(30, createNumberItem(9));
        inventory.setItem(38, createNumberItem(0));

        inventory.setItem(14, createConfirmItem());
        inventory.setItem(15, createClearItem());
        inventory.setItem(16, createBackItem());

        inventory.setItem(4, createCurrentPinDisplay(""));

        fillEmptySlots(inventory);

        openInventories.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);
    }

    private ItemStack createNumberItem(int number) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            String displayName = plugin.getConfigManager().getColoredMessage("gui.pin-number-name", "{number}", String.valueOf(number));
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createConfirmItem() {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getColoredMessage("gui.pin-confirm-name"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createClearItem() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getColoredMessage("gui.pin-clear-name"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.GRAY_WOOL);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getColoredMessage("gui.pin-back-name"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCurrentPinDisplay(String currentPin) {
        ItemStack item = new ItemStack(Material.PAPER);
        var meta = item.getItemMeta();
        if (meta != null) {
            String display = currentPin.isEmpty() ? "----" : currentPin.replaceAll(".", "*");
            meta.setDisplayName(plugin.getConfigManager().getColoredMessage("gui.pin-current", "{pin}", display));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public void handleClick(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        if (!pinInputs.containsKey(uuid)) {
            return;
        }

        StringBuilder currentPin = pinInputs.get(uuid);
        int pinLength = plugin.getConfigManager().getConfig().getInt("security.pin-length", 4);
        Inventory inventory = openInventories.get(uuid);

        int number = getNumberFromSlot(slot);
        if (number >= 0 && currentPin.length() < pinLength) {
            currentPin.append(number);
            if (inventory != null) {
                inventory.setItem(4, createCurrentPinDisplay(currentPin.toString()));
            }
        }

        if (slot == 14) {
            String ip = player.getAddress().getAddress().getHostAddress();

            if (plugin.isIpPinLocked(ip)) {
                long remaining = plugin.getRemainingIpPinLockTime(ip);
                player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.ip-pin-locked", "{time}", String.valueOf(remaining)));
                return;
            }

            if (plugin.isPinLocked(player)) {
                long remaining = plugin.getRemainingPinLockTime(player);
                player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-locked", "{time}", String.valueOf(remaining)));
                return;
            }
            
            if (currentPin.length() == pinLength) {
                if (plugin.getDatabaseManager().verifyPin(uuid, currentPin.toString())) {
                    plugin.verifyPin(player);
                    plugin.clearPinAttempts(player);
                    plugin.clearIpPinAttempts(ip);
                    player.closeInventory();
                    pinInputs.remove(uuid);
                    openInventories.remove(uuid);

                    player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-verified"));
                    
                    if (!plugin.isAuthenticated(player)) {
                        boolean isRegistered = plugin.getDatabaseManager().isRegistered(uuid);
                        if (isRegistered) {
                            sendLoginTitle(player);
                        } else {
                            sendRegisterTitle(player);
                        }
                    } else {
                        sendSuccessTitle(player);
                        if (plugin.getConfigManager().getConfig().getBoolean("discord.enabled", false)) {
                            plugin.getDiscordWebhook().sendLoginNotification(player.getName(), ip, false);
                        }
                    }
                } else {
                    plugin.incrementPinAttempt(player);
                    plugin.incrementIpPinAttempt(ip);
                    currentPin.setLength(0);
                    if (inventory != null) {
                        inventory.setItem(4, createCurrentPinDisplay(""));
                    }
                    
                    int remainingPlayer = plugin.getRemainingPinAttempts(player);
                    int remainingIp = plugin.getRemainingIpPinAttempts(ip);
                    int remaining = Math.min(remainingPlayer, remainingIp);

                    if (remainingIp <= 0 || remainingPlayer <= 0) {
                        plugin.forceIpPinLock(ip);
                        long lockTime = plugin.getRemainingIpPinLockTime(ip);
                        player.closeInventory();
                        boolean kickEnabled = plugin.getConfigManager().getConfig().getBoolean("security.ip-block-kick-enabled", true);
                        if (kickEnabled) {
                            String kickMessage = plugin.getConfigManager().getColoredMessage("messages.block-ip-pin-locked", "{time}", formatTime(lockTime));
                            player.kickPlayer(kickMessage);
                        } else {
                            player.sendMessage(plugin.getConfigManager().getColoredMessage("messages.ip-pin-locked", "{time}", String.valueOf(lockTime)));
                        }
                    } else {
                        player.sendMessage(plugin.getConfigManager().getPrefixedMessage("messages.pin-wrong-attempts", "{attempts}", String.valueOf(remaining)));
                    }
                }
            }
        }

        if (slot == 15) {
            currentPin.setLength(0);
            if (inventory != null) {
                inventory.setItem(4, createCurrentPinDisplay(""));
            }
        }

        if (slot == 16) {
            if (currentPin.length() > 0) {
                currentPin.deleteCharAt(currentPin.length() - 1);
                if (inventory != null) {
                    inventory.setItem(4, createCurrentPinDisplay(currentPin.toString()));
                }
            }
        }
    }

    private int getNumberFromSlot(int slot) {
        switch (slot) {
            case 10: return 1;
            case 11: return 2;
            case 12: return 3;
            case 19: return 4;
            case 20: return 5;
            case 21: return 6;
            case 28: return 7;
            case 29: return 8;
            case 30: return 9;
            case 38: return 0;
            default: return -1;
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

    private void sendLoginTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.login.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.login.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.login.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.login.stay", 70);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.login.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void sendRegisterTitle(Player player) {
        String title = plugin.getConfigManager().getColoredMessage("titles.register.title");
        String subtitle = plugin.getConfigManager().getColoredMessage("titles.register.subtitle");
        int fadeIn = plugin.getConfigManager().getConfig().getInt("titles.register.fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("titles.register.stay", 70);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("titles.register.fade-out", 20);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void handleClose(Player player) {
        UUID uuid = player.getUniqueId();
        if (openInventories.containsKey(uuid) && !plugin.isPinVerified(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !plugin.isPinVerified(player)) {
                    openPinGui(player);
                }
            }, 5L);
        }
    }

    public boolean hasOpenGui(Player player) {
        return openInventories.containsKey(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        pinInputs.remove(player.getUniqueId());
        openInventories.remove(player.getUniqueId());
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
