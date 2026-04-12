package com.bloom.core.mixin;

import com.bloom.core.module.modules.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the scoreboard sidebar render when Hide Scoreboard module is enabled.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD"), cancellable = true)
    private void bloomHideScoreboard(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Scoreboard.hideScoreboard) {
            ci.cancel();
        }
    }
}
