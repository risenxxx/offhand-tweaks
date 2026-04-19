package com.offhandtweaks.events;

import com.offhandtweaks.OffhandTweaks;
import com.offhandtweaks.config.ClientConfig;
import com.offhandtweaks.network.ConfigSyncPacket;
import com.offhandtweaks.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client-only forge-bus listeners.
 *
 *   - On {@link ClientPlayerNetworkEvent.LoggingIn} we push the local config state to the server
 *     so its {@code ServerConfigCache} is populated before the first interaction.
 *   - {@link #sendConfigToServerIfConnected()} is called from {@link ClientConfig}'s reload
 *     listener to re-push mid-session after the player edits their TOML.
 *
 * All of this class is guarded {@link OnlyIn @OnlyIn(Dist.CLIENT)} — it's wired up from the
 * mod entry point via {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)} so it is never
 * class-loaded on a dedicated server.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        // Send immediately. If the channel is not registered by the remote end (vanilla server),
        // the packet is dropped silently because the channel is marked optional.
        sendConfigToServerIfConnected();
    }

    public static void sendConfigToServerIfConnected() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            // Not connected yet; the upcoming LoggingIn event will push once we are.
            return;
        }
        try {
            NetworkHandler.sendToServer(new ConfigSyncPacket(ClientConfig.snapshot()));
        } catch (Exception e) {
            // The only realistic failure is that the server doesn't have our channel
            // (vanilla server). That's fine — cancellation still works client-side.
            OffhandTweaks.LOGGER.debug("Offhand Tweaks config sync skipped: {}", e.getMessage());
        }
    }
}
