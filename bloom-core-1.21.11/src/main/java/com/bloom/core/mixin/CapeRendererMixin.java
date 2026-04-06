package com.bloom.core.mixin;

import com.bloom.core.module.modules.CosmeticsCape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class CapeRendererMixin {
    @Unique
    private static String lastRegisteredCape = null;

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        if (!CosmeticsCape.showCape) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        PlayerListEntry self = (PlayerListEntry) (Object) this;
        if (self.getProfile().id().equals(client.player.getUuid())) {
            Identifier capePath = Identifier.of("bloom-core", "textures/cape/" + CosmeticsCape.capeFile);

            // Register texture if changed or first use
            if (!CosmeticsCape.capeFile.equals(lastRegisteredCape)) {
                client.getTextureManager().registerTexture(capePath, new ResourceTexture(capePath));
                lastRegisteredCape = CosmeticsCape.capeFile;
            }

            SkinTextures original = cir.getReturnValue();
            AssetInfo.TextureAssetInfo bloomCape = new AssetInfo.TextureAssetInfo(capePath, capePath);
            cir.setReturnValue(SkinTextures.create(
                original.body(),
                bloomCape,
                bloomCape,
                original.model()
            ));
        }
    }
}
