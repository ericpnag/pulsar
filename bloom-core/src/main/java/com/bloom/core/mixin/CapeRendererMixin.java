package com.bloom.core.mixin;

import com.bloom.core.module.modules.CosmeticsCape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class CapeRendererMixin {
    @Unique
    private static String lastRegisteredCape = null;

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        if (!CosmeticsCape.showCape) return;
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && player.getUuid().equals(client.player.getUuid())) {
            Identifier capePath = Identifier.of("bloom-core", "textures/cape/" + CosmeticsCape.capeFile);

            if (!CosmeticsCape.capeFile.equals(lastRegisteredCape)) {
                client.getTextureManager().registerTexture(capePath, new ResourceTexture(capePath));
                lastRegisteredCape = CosmeticsCape.capeFile;
            }

            SkinTextures original = cir.getReturnValue();
            cir.setReturnValue(new SkinTextures(
                original.texture(),
                original.textureUrl(),
                capePath,
                capePath,
                original.model(),
                original.secure()
            ));
        }
    }
}
