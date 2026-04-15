package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class ArmorStatus extends Module {
    public ArmorStatus() { super("Armor Status", "Shows equipped armor durability", false); }
    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 48; }
    @Override public void renderHud(MinecraftClient client, int y) {
        if (client.player == null) return;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = client.player.inventory.armor[i];
            if (stack != null) {
                String name = stack.getCustomName();
                if (name == null) name = "Armor";
                if (name.length() > 12) name = name.substring(0, 12);
                int dur = stack.getMaxDamage() > 0 ? (int)((1.0f - (float)stack.getDamage() / stack.getMaxDamage()) * 100) : 100;
                int color = dur > 50 ? 0x55FF55 : dur > 25 ? 0xFFFF55 : 0xFF5555;
                client.inGameHud.fill(2, y - 1, 90, y + 10, 0x44000000);
                client.textRenderer.drawWithShadow(name + " " + dur + "%", 6, y, color);
                y += 12;
            }
        }
    }
}
