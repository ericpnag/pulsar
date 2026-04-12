package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionTimer extends Module {
    public PotionTimer() {
        super("Potion Timer", "Shows potion effects with countdown", false);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 0; } // renders on right side

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null) return;
        int screenW = client.getWindow().getScaledWidth();
        int drawY = 24;

        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier();
            int totalSecs = effect.getDuration() / 20;
            int mins = totalSecs / 60;
            int secs = totalSecs % 60;

            // Amplifier display (I, II, III, etc.)
            String ampStr = amp > 0 ? " " + toRoman(amp + 1) : "";
            String timeStr = mins > 0 ? String.format("%d:%02d", mins, secs) : secs + "s";
            String text = name + ampStr;

            boolean beneficial = effect.getEffectType().value().isBeneficial();
            int nameColor = beneficial ? 0xFF98C379 : 0xFFE06C75;
            int timeColor = totalSecs <= 10 ? 0xFFE06C75 : 0xFFABB2BF;

            int tw = client.textRenderer.getWidth(text);
            int ttw = client.textRenderer.getWidth(timeStr);
            int totalW = Math.max(tw, ttw) + 12;

            int x = screenW - totalW - 4;

            // Background
            context.fill(x - 2, drawY - 2, screenW - 2, drawY + 20, 0x55000000);

            // Left accent bar
            context.fill(x - 2, drawY - 2, x, drawY + 20, beneficial ? 0xAA98C379 : 0xAAE06C75);

            // Name
            context.drawText(client.textRenderer, text, x + 4, drawY, nameColor, true);

            // Timer
            context.drawText(client.textRenderer, timeStr, x + 4, drawY + 10, timeColor, true);

            drawY += 24;
        }
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}
