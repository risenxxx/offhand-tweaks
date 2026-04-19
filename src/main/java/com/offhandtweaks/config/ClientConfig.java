package com.offhandtweaks.config;

import com.offhandtweaks.OffhandTweaks;
import com.offhandtweaks.events.ClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Client-side Forge Config. Each player owns their own copy in
 * {@code config/offhandtweaks-client.toml}. Defaults are "blocked" per the spec —
 * users must opt into vanilla behavior.
 *
 * Any change to the spec fires {@link ModConfigEvent.Reloading}; we use that to
 * re-sync the current state to the server so the authoritative side stays in
 * agreement with what the client is canceling locally.
 */
public final class ClientConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_SHIELD_RMB;
    public static final ForgeConfigSpec.BooleanValue ALLOW_LIGHT_SOURCES_RMB;
    public static final ForgeConfigSpec.BooleanValue ALLOW_FOOD_RMB;
    public static final ForgeConfigSpec.BooleanValue ALLOW_OTHER_BLOCKS_RMB;

    static {
        Pair<Holder, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Holder::new);
        SPEC = pair.getRight();
        Holder h = pair.getLeft();
        ALLOW_SHIELD_RMB = h.allowShieldRMB;
        ALLOW_LIGHT_SOURCES_RMB = h.allowLightSourcesRMB;
        ALLOW_FOOD_RMB = h.allowFoodRMB;
        ALLOW_OTHER_BLOCKS_RMB = h.allowOtherBlocksRMB;
    }

    private ClientConfig() {}

    /** Snapshot the current spec values into an immutable record for equality and network transport. */
    public static PlayerConfigState snapshot() {
        return new PlayerConfigState(
                ALLOW_SHIELD_RMB.get(),
                ALLOW_LIGHT_SOURCES_RMB.get(),
                ALLOW_FOOD_RMB.get(),
                ALLOW_OTHER_BLOCKS_RMB.get()
        );
    }

    /**
     * Mod-bus listener. Fires for both Loading and Reloading on the CLIENT spec.
     * When the player is already connected to a server, we push the new state.
     */
    public static void onConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;

        // The CLIENT config only exists on the physical client, but the event can still fire
        // during datagen or on a dedicated server if classloading quirks occur. Gate on env.
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        OffhandTweaks.LOGGER.debug("Offhand Tweaks client config {}: {}",
                event instanceof ModConfigEvent.Reloading ? "reloaded" : "loaded",
                snapshot());

        // If the game is already running and connected, re-push. If not, login will handle it.
        ClientEvents.sendConfigToServerIfConnected();
    }

    private static final class Holder {
        final ForgeConfigSpec.BooleanValue allowShieldRMB;
        final ForgeConfigSpec.BooleanValue allowLightSourcesRMB;
        final ForgeConfigSpec.BooleanValue allowFoodRMB;
        final ForgeConfigSpec.BooleanValue allowOtherBlocksRMB;

        Holder(ForgeConfigSpec.Builder builder) {
            // No push/pop — toggles live at the top level so GUI mods like Configured
            // display them on the first page without a nested "offhand_rmb" subsection.
            builder.comment("Offhand Tweaks — client-side right-click behavior toggles.",
                            "Set to true to restore vanilla behavior for that category, false to block.");

            allowShieldRMB = builder
                    .comment("Allow raising a shield via offhand RMB.")
                    .translation("offhandtweaks.config.allowShieldRMB")
                    .define("allowShieldRMB", true);

            allowLightSourcesRMB = builder
                    .comment("Allow placing / using light sources (torches, lanterns, candles, etc.) via offhand RMB.",
                             "Extendable by datapack tag: offhandtweaks:light_sources.")
                    .translation("offhandtweaks.config.allowLightSourcesRMB")
                    .define("allowLightSourcesRMB", false);

            allowFoodRMB = builder
                    .comment("Allow eating food via offhand RMB.")
                    .translation("offhandtweaks.config.allowFoodRMB")
                    .define("allowFoodRMB", true);

            allowOtherBlocksRMB = builder
                    .comment("Allow using/placing any other offhand item via RMB (not covered by the categories above).")
                    .translation("offhandtweaks.config.allowOtherBlocksRMB")
                    .define("allowOtherBlocksRMB", false);
        }
    }
}
