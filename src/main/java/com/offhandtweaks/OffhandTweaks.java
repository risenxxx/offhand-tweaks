package com.offhandtweaks;

import com.offhandtweaks.config.ClientConfig;
import com.offhandtweaks.events.ClientEvents;
import com.offhandtweaks.events.OffhandInteractionHandler;
import com.offhandtweaks.events.PlayerLifecycleHandler;
import com.offhandtweaks.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Entry point for Offhand Tweaks.
 *
 * Responsibilities:
 *   - Register the client-side config spec (each player configures their own preferences).
 *   - Register the {@link NetworkHandler} so the client can push its config state to the server.
 *   - Hook the forge bus for cancellation and player-lifecycle events.
 *   - On the client only, wire up the login-time sync hook.
 */
@Mod(OffhandTweaks.MODID)
public final class OffhandTweaks {
    public static final String MODID = "offhandtweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OffhandTweaks() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // CLIENT config — each player's machine owns the source of truth for their preferences.
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // Mod bus: common setup is where we register packet handlers (needs to happen before world load).
        modBus.addListener(this::onCommonSetup);

        // Mod bus: reacting to config load/reload so we can push updated state to the server mid-session.
        modBus.addListener(ClientConfig::onConfigEvent);

        // Forge event bus: runtime gameplay events.
        MinecraftForge.EVENT_BUS.register(OffhandInteractionHandler.class);
        MinecraftForge.EVENT_BUS.register(PlayerLifecycleHandler.class);

        // Client-only listeners (login, reload-to-resync).
        // Plain `if` relies on the JVM's lazy classloading: ClientEvents is only resolved on CLIENT.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEvents.register();
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        LOGGER.info("Offhand Tweaks network handler registered.");
    }
}
