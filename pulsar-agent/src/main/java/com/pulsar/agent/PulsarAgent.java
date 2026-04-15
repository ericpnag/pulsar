package com.pulsar.agent;

import java.lang.instrument.Instrumentation;

/**
 * Pulsar Client Java Agent for Minecraft 1.8.9
 *
 * Injects Pulsar mods into vanilla MC without Fabric/Forge.
 * Uses ASM to hook into Minecraft's rendering and tick loops.
 * This approach works on macOS ARM because it doesn't replace LWJGL.
 */
public class PulsarAgent {
    public static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("[Pulsar] Agent loaded — injecting mods into 1.8.9...");

        // Register class transformer that hooks into MC classes
        inst.addTransformer(new PulsarTransformer(), true);

        // Initialize module system
        PulsarModules.init();

        System.out.println("[Pulsar] " + PulsarModules.getModules().size() + " modules registered");
    }

    // Fallback for dynamic attach
    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }
}
