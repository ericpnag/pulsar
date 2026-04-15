package com.pulsar.modules;
import com.pulsar.agent.MCReflect;
public class FullbrightModule extends PulsarModule {
    public FullbrightModule() { super("Fullbright", false); }
    @Override public void onTick() {
        try {
            Object mc = MCReflect.getMinecraft();
            java.lang.reflect.Field gsField = mc.getClass().getField("t"); // gameSettings
            Object gs = gsField.get(mc);
            java.lang.reflect.Field gammaField = gs.getClass().getField("aF"); // gammaSetting
            gammaField.setFloat(gs, 15.0f);
        } catch (Exception ignored) {}
    }
}
