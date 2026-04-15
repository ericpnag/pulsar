package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.util.Identifier;

public class MotionBlur extends Module {
    public static float strength = 0.5f;
    private static ManagedShaderEffect shaderEffect;
    private static boolean shaderRegistered = false;

    public MotionBlur() {
        super("Motion Blur", "Real post-processing motion blur via shaders", false);
    }

    public static void initShader() {
        if (shaderRegistered) return;
        shaderEffect = ShaderEffectManager.getInstance().manage(
            Identifier.of("bloom-core", "shaders/post/motion_blur.json"),
            shader -> shader.setUniformValue("BlendFactor", strength)
        );

        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (shaderEffect != null && isActive()) {
                shaderEffect.setUniformValue("BlendFactor", strength);
                shaderEffect.render(tickDelta);
            }
        });
        shaderRegistered = true;
    }

    private static boolean isActive() {
        try {
            var modules = com.bloom.core.BloomCore.MODULES;
            if (modules == null) return false;
            var mod = modules.getByName("Motion Blur");
            return mod != null && mod.isEnabled();
        } catch (Exception e) { return false; }
    }

    @Override public boolean hasHud() { return false; }
}
