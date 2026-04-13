package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import static com.bloom.core.gui.BloomGui.*;

public class ItemCounter extends Module {
    public ItemCounter() {
        super("Item Counter", "Shows count of held item type in inventory", false);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null) return;
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) return;

        int count = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (ItemStack.areItemsEqual(stack, held)) {
                count += stack.getCount();
            }
        }

        String name = held.getName().getString();
        if (name.length() > 16) name = name.substring(0, 16) + "..";
        context.drawText(client.textRenderer, text(name + ": " + count, 0xC0C0C0), 4, y, -1, true);
    }
}
