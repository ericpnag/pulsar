package com.bloom.core.mixin;

import com.bloom.core.module.modules.TimeChanger;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Override world time of day client-side when TimeChanger is active.
 * Defaults to noon (6000) so the world looks bright.
 */
@Mixin(World.class)
public class WorldTimeMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void bloomTimeChanger(CallbackInfoReturnable<Long> cir) {
        if (TimeChanger.active) {
            cir.setReturnValue(TimeChanger.clientTime);
        }
    }
}
