package com.bloom.core.mixin;

import com.bloom.core.BloomCore;
import com.bloom.core.module.modules.DropProtection;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class DropMixin {
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (BloomCore.MODULES != null) {
            var mod = BloomCore.MODULES.getByName("Drop Protection");
            if (mod != null && mod.isEnabled() && DropProtection.preventDrop) {
                cir.setReturnValue(false);
            }
        }
    }
}
