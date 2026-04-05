package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorStatus extends Module {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public ArmorStatus() {
        super("Armor Status", "Show armor durability on screen", false);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null) return;
        int screenW = client.getWindow().getScaledWidth();
        int drawY = client.getWindow().getScaledHeight() / 2 - 40;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = client.player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            int max = stack.getMaxDamage();
            if (max == 0) continue;
            int remaining = max - stack.getDamage();
            int pct = (remaining * 100) / max;
            int color = pct > 50 ? 0xFF55DD88 : pct > 25 ? 0xFFDDBB55 : 0xFFDD5566;

            String text = remaining + "";
            int tw = client.textRenderer.getWidth(text);
            context.fill(screenW - tw - 28, drawY, screenW - 4, drawY + 18, 0x44000000);
            context.drawItem(stack, screenW - 20, drawY);
            context.drawText(client.textRenderer, text, screenW - tw - 24, drawY + 5, color, true);
            drawY += 20;
        }
    }
}
