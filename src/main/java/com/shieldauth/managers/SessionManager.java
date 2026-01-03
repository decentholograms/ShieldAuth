package com.shieldauth.managers;

import com.shieldauth.ShieldAuth;

import java.util.UUID;

public class SessionManager {

    private final ShieldAuth plugin;

    public SessionManager(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    public boolean hasValidSession(UUID uuid, String ip) {
        return plugin.getDatabaseManager().hasValidSession(uuid, ip);
    }

    public void saveSession(UUID uuid, String ip) {
        plugin.getDatabaseManager().saveSession(uuid, ip);
    }

    public void invalidateSession(UUID uuid) {
        plugin.getDatabaseManager().deleteSession(uuid);
    }
}
