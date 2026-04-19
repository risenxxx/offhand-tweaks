package com.offhandtweaks.network;

import com.offhandtweaks.OffhandTweaks;
import com.offhandtweaks.config.PlayerConfigState;
import com.offhandtweaks.config.ServerConfigCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server packet carrying the sender's current {@link PlayerConfigState}.
 *
 * Sent on:
 *   - client login, so the server has something to consult before the first interaction
 *   - any subsequent config reload, so cancellation stays consistent mid-session
 */
public final class ConfigSyncPacket {

    private final PlayerConfigState state;

    public ConfigSyncPacket(PlayerConfigState state) {
        this.state = state;
    }

    public static void encode(ConfigSyncPacket pkt, FriendlyByteBuf buf) {
        pkt.state.write(buf);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        return new ConfigSyncPacket(PlayerConfigState.read(buf));
    }

    public static void handle(ConfigSyncPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return; // packet on the wrong side — ignore
            ServerConfigCache.put(sender.getUUID(), pkt.state);
            OffhandTweaks.LOGGER.debug("Received offhand config from {}: {}", sender.getGameProfile().getName(), pkt.state);
        });
        ctx.setPacketHandled(true);
    }
}
