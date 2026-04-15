package com.pulsar.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * ASM transformer that hooks into Minecraft 1.8.9.
 *
 * Strategy: Hook into ALL classes looking for the right patterns,
 * rather than relying on specific obfuscated names.
 */
public class PulsarTransformer implements ClassFileTransformer {
    private boolean hookedTick = false;
    private boolean hookedHud = false;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null || classfileBuffer == null) return null;

        try {
            // Hook into Minecraft main class (ave) for tick
            if (className.equals("ave") && !hookedTick) {
                return hookMinecraft(classfileBuffer);
            }

            // Hook into GuiIngame (avo) for HUD rendering
            if (className.equals("avo") && !hookedHud) {
                return hookGuiIngame(classfileBuffer);
            }
        } catch (Exception e) {
            System.err.println("[Pulsar] Transform error on " + className + ": " + e.getMessage());
        }
        return null;
    }

    private byte[] hookMinecraft(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            // Hook into EVERY private void no-arg method in Minecraft class
            // One of them is runGameLoop — our onTick call is lightweight so calling it extra times is fine
            if (method.name.equals("at") && method.desc.equals("()V")) {
                // at() is a common candidate for runTick
                injectTickHook(method);
                hookedTick = true;
                System.out.println("[Pulsar] Hooked ave.at() (tick candidate)");
            }

            // Also try 'as' which is runGameLoop in some mappings
            if (method.name.equals("as") && method.desc.equals("()V")) {
                injectTickHook(method);
                hookedTick = true;
                System.out.println("[Pulsar] Hooked ave.as() (tick candidate)");
            }

            // Hook into 'av' - another common candidate
            if (method.name.equals("av") && method.desc.equals("()V")) {
                injectTickHook(method);
                hookedTick = true;
                System.out.println("[Pulsar] Hooked ave.av() (tick candidate)");
            }

            // Hook click methods to block when menu is open
            // an = clickMouse, ao = rightClickMouse, aw = middleClickMouse
            for (String clickMethod : new String[]{"an", "ao", "aw"}) {
                for (MethodNode mn : classNode.methods) {
                    if (mn.name.equals(clickMethod) && mn.desc.equals("()V")) {
                        InsnList check = new InsnList();
                        check.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "com/pulsar/agent/PulsarModules", "isMenuOpen", "()Z", false));
                        LabelNode continueLabel = new LabelNode();
                        check.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
                        check.add(new InsnNode(Opcodes.RETURN));
                        check.add(continueLabel);
                        mn.instructions.insertBefore(mn.instructions.getFirst(), check);
                        System.out.println("[Pulsar] Blocked clicks in ave." + clickMethod + "()");
                        break;
                    }
                }
            }
        }

        if (!hookedTick) {
            // Fallback: hook the first private void no-arg method
            for (MethodNode method : classNode.methods) {
                if (method.desc.equals("()V") && (method.access & Opcodes.ACC_PRIVATE) != 0
                    && !method.name.equals("<init>") && !method.name.equals("<clinit>")) {
                    injectTickHook(method);
                    hookedTick = true;
                    System.out.println("[Pulsar] Hooked ave." + method.name + "() (fallback tick)");
                    break;
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    /**
     * Hook into avo.a(F)V — GuiIngame.renderGameOverlay(float)
     */
    private byte[] hookGuiIngame(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            // a(F)V = renderGameOverlay(float partialTicks)
            if (method.name.equals("a") && method.desc.equals("(F)V")) {
                InsnList inject = new InsnList();
                inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/pulsar/agent/PulsarModules", "onRenderHud", "()V", false));

                // Insert before last RETURN
                AbstractInsnNode lastReturn = null;
                for (AbstractInsnNode node : method.instructions) {
                    if (node.getOpcode() == Opcodes.RETURN) lastReturn = node;
                }
                if (lastReturn != null) {
                    method.instructions.insertBefore(lastReturn, inject);
                }
                hookedHud = true;
                System.out.println("[Pulsar] Hooked avo.a(F)V — HUD rendering active!");
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private void injectTickHook(MethodNode method) {
        InsnList inject = new InsnList();
        inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
            "com/pulsar/agent/PulsarModules", "onTick", "()V", false));
        method.instructions.insertBefore(method.instructions.getFirst(), inject);
    }

    private void injectRenderHook(MethodNode method) {
        InsnList inject = new InsnList();
        inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
            "com/pulsar/agent/PulsarModules", "onRenderHud", "()V", false));
        // Insert before last RETURN
        AbstractInsnNode lastReturn = null;
        for (AbstractInsnNode node : method.instructions) {
            if (node.getOpcode() == Opcodes.RETURN) lastReturn = node;
        }
        if (lastReturn != null) {
            method.instructions.insertBefore(lastReturn, inject);
        }
    }
}
