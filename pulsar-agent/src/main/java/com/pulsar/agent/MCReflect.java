package com.pulsar.agent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection helper for obfuscated Minecraft 1.8.9 classes.
 *
 * Correct mappings (verified from JAR):
 *   ave = Minecraft
 *   ave.A() = getMinecraft (static)
 *   ave.C() = getDebugFPS (static, returns int)
 *   ave.h = thePlayer (bew = EntityPlayerSP)
 *   ave.k = fontRendererObj (avn = FontRenderer)
 *   avn.a(String, float, float, int, boolean) = drawString (boolean=shadow)
 *   pk = Entity base class
 *   pk.p, pk.q, pk.r = posX, posY, posZ
 *   pk.z = rotationYaw (float)
 */
public class MCReflect {
    public static Method getMC;
    public static Field debugFPSField;
    public static Method drawStringMethod;
    public static Field thePlayerField;
    public static Field fontRendererField;
    public static Field posXField, posYField, posZField;
    public static Field yawField;

    private static boolean initialized = false;
    private static boolean failed = false;

    public static boolean init() {
        if (initialized) return true;
        if (failed) return false;
        try {
            Class<?> mcClass = Class.forName("ave");
            getMC = mcClass.getMethod("A");                    // getMinecraft
            debugFPSField = mcClass.getDeclaredField("ao");    // debugFPS (private static int)
            debugFPSField.setAccessible(true);
            thePlayerField = mcClass.getField("h");            // thePlayer
            fontRendererField = mcClass.getField("k");         // fontRendererObj

            Class<?> fontClass = Class.forName("avn");
            // Try drawStringWithShadow: a(String, float, float, int, boolean)
            try {
                drawStringMethod = fontClass.getMethod("a", String.class, float.class, float.class, int.class, boolean.class);
                System.out.println("[Pulsar] Using drawString(String,float,float,int,boolean)");
            } catch (NoSuchMethodException e) {
                // Fallback: a(String, int, int, int)
                drawStringMethod = fontClass.getMethod("a", String.class, int.class, int.class, int.class);
                System.out.println("[Pulsar] Using drawString(String,int,int,int)");
            }

            Class<?> entityClass = Class.forName("pk");
            posXField = entityClass.getField("p");              // posX
            posYField = entityClass.getField("q");              // posY
            posZField = entityClass.getField("r");              // posZ
            yawField = entityClass.getField("z");               // rotationYaw

            initialized = true;
            System.out.println("[Pulsar] MC reflection initialized — all hooks ready");
            return true;
        } catch (Exception e) {
            System.err.println("[Pulsar] Failed to init MC reflection: " + e.getMessage());
            failed = true;
            return false;
        }
    }

    public static Object getMinecraft() throws Exception {
        return getMC.invoke(null);
    }

    public static Object getPlayer() throws Exception {
        return thePlayerField.get(getMinecraft());
    }

    public static boolean hasPlayer() {
        try { return getPlayer() != null; } catch (Exception e) { return false; }
    }

    public static void drawText(String text, float x, float y, int color) {
        try {
            Object font = fontRendererField.get(getMinecraft());
            if (font == null) return;
            // Add full alpha to color if not present
            if ((color & 0xFF000000) == 0) color |= 0xFF000000;
            int paramCount = drawStringMethod.getParameterCount();
            if (paramCount == 5) {
                drawStringMethod.invoke(font, text, x, y, color, true);
            } else {
                drawStringMethod.invoke(font, text, (int)x, (int)y, color);
            }
        } catch (Exception e) {
            // Silent — don't spam logs
        }
    }

    public static int getFPS() {
        try { return debugFPSField.getInt(null); } catch (Exception e) { return 0; }
    }

    public static double getPlayerX() {
        try { return posXField.getDouble(getPlayer()); } catch (Exception e) { return 0; }
    }
    public static double getPlayerY() {
        try { return posYField.getDouble(getPlayer()); } catch (Exception e) { return 0; }
    }
    public static double getPlayerZ() {
        try { return posZField.getDouble(getPlayer()); } catch (Exception e) { return 0; }
    }
    public static float getPlayerYaw() {
        try { return yawField.getFloat(getPlayer()); } catch (Exception e) { return 0; }
    }
}
