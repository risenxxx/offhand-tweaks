package com.offhandtweaks.config;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Immutable snapshot of one player's offhand preferences.
 *
 * Used in three places:
 *   - Client-side: built from {@link ClientConfig} on demand.
 *   - In flight:   serialised via {@link #write(FriendlyByteBuf)} in the sync packet.
 *   - Server-side: stashed per UUID in {@link ServerConfigCache}.
 */
public record PlayerConfigState(
        boolean allowShieldRMB,
        boolean allowLightSourcesRMB,
        boolean allowFoodRMB,
        boolean allowOtherBlocksRMB
) {

    /**
     * Fall-open defaults used when the server hasn't received a sync yet (e.g. vanilla client).
     * We choose "allow everything" so non-modded clients are never blocked by an absent packet.
     */
    public static PlayerConfigState vanillaDefaults() {
        return new PlayerConfigState(true, true, true, true);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(allowShieldRMB);
        buf.writeBoolean(allowLightSourcesRMB);
        buf.writeBoolean(allowFoodRMB);
        buf.writeBoolean(allowOtherBlocksRMB);
    }

    public static PlayerConfigState read(FriendlyByteBuf buf) {
        return new PlayerConfigState(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }
}
