package com.bloom.core.mixin;

import com.bloom.core.module.modules.WeatherChanger;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WeatherMixin {
    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
    private void bloomClearWeather(float delta, CallbackInfoReturnable<Float> cir) {
        if (WeatherChanger.active) {
            cir.setReturnValue(0.0f);
        }
    }
}
