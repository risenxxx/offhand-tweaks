package com.offhandtweaks.events;

import com.offhandtweaks.config.ClientConfig;
import com.offhandtweaks.config.PlayerConfigState;
import com.offhandtweaks.config.ServerConfigCache;
import com.offhandtweaks.util.ItemCategorizer;
import com.offhandtweaks.util.ItemCategorizer.Category;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * Forge-bus subscriber that cancels offhand right-click interactions the player has disabled.
 *
 * We run on BOTH logical sides to prevent desync:
 *   - Client side: cancels prediction so there is no ghost placement, no swing animation.
 *   - Server side: cancels the authoritative action so nothing is applied to the world.
 *
 * Each side consults its own copy of the config:
 *   - Client reads the local {@link ClientConfig} directly.
 *   - Server reads the latest {@link PlayerConfigState} synced from that specific player.
 *
 * HIGHEST priority ensures we cancel before other mods' handlers run (e.g. mods that
 * would otherwise consume the event and react to a shield/food item we meant to block).
 */
public final class OffhandInteractionHandler {

    private OffhandInteractionHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        maybeCancel(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        maybeCancel(event);
    }

    private static void maybeCancel(PlayerInteractEvent event) {
        // Only the offhand is our concern. Main-hand interactions are left fully alone.
        if (event.getHand() != InteractionHand.OFF_HAND) return;

        ItemStack stack = event.getItemStack();
        Category category = ItemCategorizer.classify(stack);
        if (category == Category.NONE) return;

        Player player = event.getEntity();
        boolean clientSide = event.getSide() == LogicalSide.CLIENT;

        PlayerConfigState state = clientSide
                ? ClientConfig.snapshot()                                   // authoritative for prediction
                : ServerConfigCache.get(player.getUUID());                  // authoritative for world state

        boolean allowed = switch (category) {
            case SHIELD       -> state.allowShieldRMB();
            case FOOD         -> state.allowFoodRMB();
            case LIGHT_SOURCE -> state.allowLightSourcesRMB();
            case OTHER        -> state.allowOtherBlocksRMB();
            default           -> true;
        };

        if (!allowed) {
            event.setCanceled(true);
            // FAIL tells vanilla not to swing the hand or play the "use" animation.
            // Crucial for keeping the client and server in lockstep on cancellation.
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}
