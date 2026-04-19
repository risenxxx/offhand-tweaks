package com.offhandtweaks.client;

import com.offhandtweaks.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * In-game config screen for Offhand Tweaks.
 *
 * Pure vanilla widgets: each toggle is a {@link CycleButton} built via
 * {@link CycleButton#onOffBuilder(boolean)}, which gives us the standard
 * green "ON" / red "OFF" styling seen in Minecraft's own option menus — no
 * custom drawing required.
 *
 * {@code BooleanValue#set} auto-persists to the TOML file and fires
 * {@code ModConfigEvent.Reloading}, which our existing listener uses to push
 * the updated state to the connected server. So toggling a button in this
 * screen is equivalent to editing the TOML by hand.
 */
@OnlyIn(Dist.CLIENT)
public final class OffhandTweaksConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 25;
    private static final int DONE_WIDTH = 200;

    private final Screen parent;

    public OffhandTweaksConfigScreen(Screen parent) {
        super(Component.translatable("offhandtweaks.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        addToggle(centerX, startY,                          ClientConfig.ALLOW_SHIELD_RMB,         "allowShieldRMB");
        addToggle(centerX, startY + BUTTON_SPACING,         ClientConfig.ALLOW_LIGHT_SOURCES_RMB,  "allowLightSourcesRMB");
        addToggle(centerX, startY + BUTTON_SPACING * 2,     ClientConfig.ALLOW_FOOD_RMB,           "allowFoodRMB");
        addToggle(centerX, startY + BUTTON_SPACING * 3,     ClientConfig.ALLOW_OTHER_BLOCKS_RMB,   "allowOtherBlocksRMB");

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), btn -> this.onClose())
                        .bounds(centerX - DONE_WIDTH / 2, this.height - 27, DONE_WIDTH, BUTTON_HEIGHT)
                        .build()
        );
    }

    private void addToggle(int centerX, int y, ForgeConfigSpec.BooleanValue value, String key) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(value.get())
                .withTooltip(v -> Tooltip.create(Component.translatable("offhandtweaks.config." + key + ".tooltip")))
                .create(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        Component.translatable("offhandtweaks.config." + key),
                        (btn, newValue) -> value.set(newValue)
                );
        this.addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
