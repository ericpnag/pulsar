package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionEffects extends Module {
    public PotionEffects() {
        super("Potion Effects", "Show active potion effects", true);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 0; } // renders on right side

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null || client.getWindow() == null) return;
        int screenW = client.getWindow().getScaledWidth();
        int drawY = 4;
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier() + 1;
            int dur = effect.getDuration() / 20;
            String text = name + (amp > 1 ? " " + amp : "") + " " + dur + "s";
            int tw = client.textRenderer.getWidth(text);
            int color = effect.getEffectType().value().isBeneficial() ? 0xFF55DD88 : 0xFFDD5566;
            context.fill(screenW - tw - 10, drawY - 1, screenW - 2, drawY + 10, 0x44000000);
            context.drawText(client.textRenderer, text, screenW - tw - 6, drawY, color, true);
            drawY += 12;
        }
    }
}
