package com.bloom.core.mixin;

import com.bloom.core.presence.BloomPresence;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders a small Pulsar icon next to the nametag of
 * players detected as Pulsar Client users.
 *
 * Approach: We inject at HEAD of renderLabelIfPresent and modify the
 * playerName / displayName fields to prepend a purple Pulsar
 * symbol. This integrates naturally with MC's label rendering pipeline
 * and works with the new command-queue renderer in 1.21.11.
 *
 * After the method runs, we restore the original names so the
 * modification is purely visual and doesn't leak into other systems.
 */
@Mixin(PlayerEntityRenderer.class)
public class NametagMixin {

    /**
     * Private Use Area character mapped to the Pulsar icon
     * via the bitmap font provider in assets/minecraft/font/default.json.
     */
    @Unique
    private static final String PULSAR_SYMBOL = "\uE100";

    /** Pulsar purple color matching the cape theme */
    @Unique
    private static final int PULSAR_PURPLE = 0xC678DD;

    @Unique
    private Text originalPlayerName;
    @Unique
    private Text originalDisplayName;

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void bloomPrependIcon(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!BloomPresence.isBloomUser(state.id)) return;

        // Save originals
        originalPlayerName = state.playerName;
        originalDisplayName = state.displayName;

        // Build the prefix: purple Pulsar icon + space
        MutableText prefix = Text.literal(PULSAR_SYMBOL + " ")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(PULSAR_PURPLE)));

        // Prepend to playerName (the main name shown above the head)
        if (state.playerName != null) {
            state.playerName = prefix.copy().append(state.playerName);
        }

        // Prepend to displayName (the scoreboard/team-formatted name, shown below)
        if (state.displayName != null) {
            state.displayName = prefix.copy().append(state.displayName);
        }
    }

    @Inject(
        method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("TAIL")
    )
    private void bloomRestoreNames(
            PlayerEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!BloomPresence.isBloomUser(state.id)) return;

        // Restore original names
        state.playerName = originalPlayerName;
        state.displayName = originalDisplayName;
        originalPlayerName = null;
        originalDisplayName = null;
    }
}
