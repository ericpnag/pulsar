package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import static com.bloom.core.gui.BloomGui.*;

public class ArrowCounter extends Module {
    public ArrowCounter() {
        super("Arrow Counter", "Shows arrow count in inventory", false);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null) return;
        int count = client.player.getInventory().count(Items.ARROW)
                  + client.player.getInventory().count(Items.SPECTRAL_ARROW)
                  + client.player.getInventory().count(Items.TIPPED_ARROW);
        context.drawText(client.textRenderer, text("Arrows: " + count, 0xC0C0C0), 4, y, -1, true);
    }
}
