package com.offhandtweaks.events;

import com.offhandtweaks.config.ServerConfigCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Keeps {@link ServerConfigCache} tidy by dropping entries when players disconnect.
 * Runs on the forge bus, server side only (PlayerEvent.PlayerLoggedOutEvent is a server event).
 */
public final class PlayerLifecycleHandler {

    private PlayerLifecycleHandler() {}

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ServerConfigCache.remove(sp.getUUID());
        }
    }
}
