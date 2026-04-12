package com.bloom.core.presence;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players are using Pulsar Client.
 * Currently only the local player is detected (safe for all servers).
 */
public class BloomPresence {

    private static final Set<UUID> BLOOM_USERS = ConcurrentHashMap.newKeySet();

    public static boolean isBloomUser(UUID uuid) {
        return BLOOM_USERS.contains(uuid);
    }

    public static boolean isBloomUser(int entityId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        net.minecraft.entity.Entity entity = client.world.getEntityById(entityId);
        if (entity == null) return false;
        return BLOOM_USERS.contains(entity.getUuid());
    }

    public static void init() {
        // On join, mark ourselves as a Pulsar user
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                BLOOM_USERS.add(client.player.getUuid());
            }
        });

        // Clear on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            BLOOM_USERS.clear();
        });
    }
}
