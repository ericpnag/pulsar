package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class SessionStats extends Module {
    private int kills = 0;
    private int deaths = 0;
    private int blocksPlaced = 0;
    private int blocksBroken = 0;
    private long sessionStartTick = -1;
    private long tickCount = 0;

    // Kill detection: track attacked entity and tick of attack
    private Entity lastAttackedEntity = null;
    private long lastAttackTick = 0;

    // Death detection: track previous dead state to detect transitions
    private boolean wasDead = false;

    // Dimension/server change detection
    private String lastDimension = null;
    private String lastServer = null;

    // Block tracking: previous held item use / mining state
    private boolean wasUsingItem = false;
    private boolean wasMining = false;

    public SessionStats() {
        super("Session Stats", "Tracks kills, deaths, and play time", false);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;

        // Initialize session on first tick
        if (sessionStartTick < 0) {
            sessionStartTick = tickCount;
        }
        tickCount++;

        // Detect dimension change
        String dim = client.world != null ? client.world.getRegistryKey().getValue().toString() : "";
        if (lastDimension != null && !lastDimension.equals(dim)) {
            reset();
        }
        lastDimension = dim;

        // Detect server change
        String server = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "singleplayer";
        if (lastServer != null && !lastServer.equals(server)) {
            reset();
        }
        lastServer = server;

        // Death detection
        boolean isDead = client.player.isDead();
        if (isDead && !wasDead) {
            deaths++;
        }
        wasDead = isDead;

        // Kill detection: check if player is swinging at an entity
        boolean swinging = client.player.handSwinging;
        if (swinging && client.crosshairTarget != null &&
            client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) client.crosshairTarget;
            Entity target = entityHit.getEntity();
            if (target instanceof LivingEntity) {
                lastAttackedEntity = target;
                lastAttackTick = tickCount;
            }
        }

        // Check if attacked entity died within 3 ticks
        if (lastAttackedEntity != null) {
            if (tickCount - lastAttackTick > 3) {
                lastAttackedEntity = null;
            } else if (lastAttackedEntity instanceof LivingEntity living) {
                if (living.isDead() || living.getHealth() <= 0) {
                    kills++;
                    lastAttackedEntity = null;
                }
            }
        }

        // Block placed detection (right click with block item while targeting block)
        boolean usingItem = client.options.useKey.isPressed();
        if (usingItem && !wasUsingItem && client.crosshairTarget != null &&
            client.crosshairTarget.getType() == HitResult.Type.BLOCK &&
            client.player.getMainHandStack() != null &&
            client.player.getMainHandStack().getItem() instanceof net.minecraft.item.BlockItem) {
            blocksPlaced++;
        }
        wasUsingItem = usingItem;

        // Block broken detection (attack key on block)
        boolean mining = client.options.attackKey.isPressed() && client.crosshairTarget != null &&
            client.crosshairTarget.getType() == HitResult.Type.BLOCK;
        if (client.interactionManager != null && wasMining && !mining) {
            // Block was broken when mining stops on a block
            blocksBroken++;
        }
        wasMining = mining;
    }

    private void reset() {
        kills = 0;
        deaths = 0;
        blocksPlaced = 0;
        blocksBroken = 0;
        sessionStartTick = tickCount;
        lastAttackedEntity = null;
        wasDead = false;
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        long elapsed = (tickCount - sessionStartTick) / 20; // ticks to seconds
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        String time = String.format("%d:%02d", minutes, seconds);

        String text;
        if (deaths > 0) {
            String kd = String.format("%.1f", (double) kills / deaths);
            text = "K: " + kills + "  D: " + deaths + "  KD: " + kd + "  Time: " + time;
        } else {
            text = "K: " + kills + "  D: " + deaths + "  Time: " + time;
        }

        int tw = client.textRenderer.getWidth(text);
        int x = 2, pad = 8;
        int bgW = tw + pad * 2 + 2;
        int color = 0xFFA0A0B0;

        // Background
        context.fill(x, y - 2, x + bgW, y + 11, 0x8C0A0A12);
        // Accent left bar
        context.fill(x, y - 2, x + 2, y + 11, color & 0x44FFFFFF);
        // Border top/bottom
        context.fill(x, y - 2, x + bgW, y - 1, 0x14FFFFFF);
        context.fill(x, y + 10, x + bgW, y + 11, 0x14FFFFFF);

        // Draw text
        context.drawText(client.textRenderer, text, x + pad, y, color, true);
    }
}
