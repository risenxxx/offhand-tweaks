package com.offhandtweaks.config;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side store of each connected player's client config.
 *
 * A player's entry appears when the client sends a {@code ConfigSyncPacket} (on login
 * and whenever their config reloads) and is removed on disconnect. If a player has
 * never synced (e.g. they have no mod installed), {@link #get(UUID)} returns
 * {@link PlayerConfigState#vanillaDefaults()} so we never block non-modded clients.
 */
public final class ServerConfigCache {

    private static final Map<UUID, PlayerConfigState> STATES = new ConcurrentHashMap<>();

    private ServerConfigCache() {}

    public static void put(UUID id, PlayerConfigState state) {
        STATES.put(id, state);
    }

    public static PlayerConfigState get(UUID id) {
        return STATES.getOrDefault(id, PlayerConfigState.vanillaDefaults());
    }

    public static void remove(UUID id) {
        STATES.remove(id);
    }

    public static boolean hasSynced(UUID id) {
        return STATES.containsKey(id);
    }
}
