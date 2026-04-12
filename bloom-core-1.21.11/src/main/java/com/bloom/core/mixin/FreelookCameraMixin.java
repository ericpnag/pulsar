package com.bloom.core.mixin;

import com.bloom.core.module.modules.Freelook;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Override camera rotation with freelook angles when active.
 */
@Mixin(Camera.class)
public abstract class FreelookCameraMixin {
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("RETURN"))
    private void bloomFreelookCamera(World world, Entity entity, boolean thirdPerson,
                                      boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (Freelook.looking) {
            setRotation(Freelook.cameraYaw, Freelook.cameraPitch);
        }
    }
}
