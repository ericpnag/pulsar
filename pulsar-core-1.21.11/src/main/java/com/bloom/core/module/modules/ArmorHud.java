package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHud extends Module {
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public ArmorHud() {
        super("Armor HUD", "Shows armor next to hotbar", false);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 0; } // renders at hotbar, not stacking

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null || client.getWindow() == null) return;
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Count non-empty armor pieces to size the display
        int count = 0;
        for (EquipmentSlot slot : SLOTS) {
            if (!client.player.getEquippedStack(slot).isEmpty()) count++;
        }
        if (count == 0) return;

        // Position: horizontal row to the left of the hotbar
        // Hotbar is 182px wide, centered. Each slot is 20px.
        int hotbarLeft = screenW / 2 - 91;
        int slotSize = 20;
        int totalW = count * slotSize + 2;
        int startX = hotbarLeft - totalW - 4;
        int startY = screenH - 22;

        // Background panel
        context.fill(startX - 1, startY - 1, startX + totalW, startY + slotSize + 1, 0xCC0A0A0F);
        // Border
        context.fill(startX - 1, startY - 1, startX + totalW, startY, 0x44C678DD);
        context.fill(startX - 1, startY + slotSize, startX + totalW, startY + slotSize + 1, 0x33C678DD);
        context.fill(startX - 1, startY, startX, startY + slotSize, 0x33C678DD);
        context.fill(startX + totalW - 1, startY, startX + totalW, startY + slotSize, 0x33C678DD);

        int drawX = startX + 1;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = client.player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            // Slot background
            context.fill(drawX, startY, drawX + slotSize - 2, startY + slotSize, 0x44000000);

            // Draw item centered in slot
            context.drawItem(stack, drawX + 1, startY + 2);

            // Durability bar under item if damaged
            int max = stack.getMaxDamage();
            if (max > 0 && stack.getDamage() > 0) {
                int remaining = max - stack.getDamage();
                float pct = (float) remaining / max;
                int barW = slotSize - 4;
                int barX = drawX + 1;
                int barY = startY + slotSize - 3;

                // Bar background
                context.fill(barX, barY, barX + barW, barY + 2, 0xFF0A0A0F);

                // Bar fill — green to red
                int g = (int) (pct * 255);
                int r = (int) ((1 - pct) * 255);
                int col = 0xFF000000 | (r << 16) | (g << 8);
                context.fill(barX, barY, barX + (int)(barW * pct), barY + 2, col);
            }

            drawX += slotSize;
        }
    }
}
