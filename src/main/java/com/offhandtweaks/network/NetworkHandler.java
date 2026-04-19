package com.offhandtweaks.network;

import com.offhandtweaks.OffhandTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Registration of the Offhand Tweaks {@link SimpleChannel}.
 *
 * Protocol is deliberately tolerant: the channel accepts any version on either
 * end, so a modded client connecting to a vanilla server does not break the
 * handshake. A vanilla server simply will not receive the packet — cancellation
 * still works on the client side (which is where animation/prediction matter).
 */
public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(OffhandTweaks.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(v -> true)
            .serverAcceptedVersions(v -> true)
            .simpleChannel();

    private NetworkHandler() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                ConfigSyncPacket.class,
                ConfigSyncPacket::encode,
                ConfigSyncPacket::decode,
                ConfigSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    /** Convenience: send a packet from the client to the server it is connected to. */
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    /** Optional reverse direction helper (unused today — kept for future extensions). */
    @SuppressWarnings("unused")
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
