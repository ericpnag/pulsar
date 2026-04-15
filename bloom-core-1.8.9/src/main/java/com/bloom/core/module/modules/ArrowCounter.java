package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArrowCounter extends Module {
    public ArrowCounter() { super("Arrow Counter", "Shows arrow count in inventory", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        if (client.player == null) return;
        int count = 0;
        for (int i = 0; i < client.player.inventory.getInvSize(); i++) {
            ItemStack stack = client.player.inventory.getInvStack(i);
            if (stack != null && stack.getItem() == Items.ARROW) count += stack.count;
        }
        String text = "Arrows: " + count;
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, 0xC0C0C0);
    }
}
