package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

public class CosmeticsHalo extends Module {
    public static boolean showHalo = false;
    private int tickCount = 0;

    public CosmeticsHalo() {
        super("Pulsar Halo", "Show a glowing halo above your head", false);
    }

    @Override
    public void onEnable() { showHalo = true; }

    @Override
    public void onDisable() { showHalo = false; }

    @Override
    public void onTick(MinecraftClient client) {
        if (!showHalo || client.player == null || client.world == null) return;

        tickCount++;

        // Spawn particles in a ring above the player's head
        double px = client.player.getX();
        double py = client.player.getY() + 2.2; // above head
        double pz = client.player.getZ();

        float radius = 0.4f;
        int particlesPerTick = 3;

        // Gentle bobbing
        py += Math.sin(tickCount * 0.08) * 0.03;

        for (int i = 0; i < particlesPerTick; i++) {
            double angle = ((tickCount * 3 + i * (360.0 / particlesPerTick)) % 360) * Math.PI / 180.0;
            double x = px + Math.cos(angle) * radius;
            double z = pz + Math.sin(angle) * radius;

            // Use END_ROD particles for a golden glow effect
            client.world.addParticleClient(
                ParticleTypes.END_ROD,
                x, py, z,
                0.0, 0.005, 0.0 // very slight upward drift
            );
        }

        // Occasional sparkle
        if (tickCount % 10 == 0) {
            double sparkleAngle = Math.random() * Math.PI * 2;
            client.world.addParticleClient(
                ParticleTypes.ENCHANT,
                px + Math.cos(sparkleAngle) * radius * 0.8,
                py + 0.1,
                pz + Math.sin(sparkleAngle) * radius * 0.8,
                0.0, -0.02, 0.0
            );
        }
    }

    @Override
    public boolean hasHud() { return false; }
}
