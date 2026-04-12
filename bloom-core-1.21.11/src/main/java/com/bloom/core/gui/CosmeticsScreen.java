package com.bloom.core.gui;

import com.bloom.core.module.modules.CosmeticsCape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.*;

public class CosmeticsScreen extends Screen {
    private final Screen parent;
    private int selectedCape = 0;
    private float playerRotation = 0;

    private static final StyleSpriteSource BLOOM_FONT = new StyleSpriteSource.Font(Identifier.of("bloom-core", "inter"));

    private static final String[] CAPE_NAMES = {
        "Nebula", "Midnight", "Frost", "Flame",
        "Ocean", "Emerald", "Sunset", "Galaxy", "None"
    };
    private static final String[] CAPE_FILES = {
        "bloom_cape.png", "midnight_cape.png", "frost_cape.png", "flame_cape.png",
        "ocean_cape.png", "emerald_cape.png", "sunset_cape.png", "galaxy_cape.png", null
    };
    private static final int[][] CAPE_TOP = {
        {255,190,210}, {30,15,50}, {140,200,255}, {255,120,40},
        {20,80,160}, {30,150,70}, {255,150,80}, {15,8,30}, {40,40,40}
    };
    private static final int[][] CAPE_BOT = {
        {215,120,150}, {15,8,30}, {80,140,220}, {200,60,20},
        {10,40,100}, {15,100,40}, {120,50,150}, {5,2,15}, {30,30,30}
    };

    public CosmeticsScreen(Screen parent) {
        super(Text.literal("Cosmetics"));
        this.parent = parent;
    }

    private Text txt(String s, int color) {
        return Text.literal(s).setStyle(Style.EMPTY.withFont(BLOOM_FONT).withColor(color));
    }

    private int textW(String s) {
        return this.textRenderer.getWidth(txt(s, 0xFFFFFF));
    }

    private void drawRoundRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void drawRoundRectOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + 1, color);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    @Override
    protected void init() {
        if (!CosmeticsCape.showCape) {
            selectedCape = CAPE_NAMES.length - 1;
        } else {
            for (int i = 0; i < CAPE_FILES.length; i++) {
                if (CAPE_FILES[i] != null && CAPE_FILES[i].equals(CosmeticsCape.capeFile)) {
                    selectedCape = i; break;
                }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int w = this.width, h = this.height, cx = w / 2;

        // Background
        ctx.fill(0, 0, w, h, 0xF00A0A0F);

        // Title area
        ctx.drawText(this.textRenderer, txt("WARDROBE", 0xC678DD), cx - textW("WARDROBE") / 2, 10, -1, false);
        ctx.drawText(this.textRenderer, txt("Customize your look", 0x5C6370), cx - textW("Customize your look") / 2, 22, -1, false);
        ctx.fill(cx - 50, 33, cx + 50, 34, 0x15C678DD);

        // === LEFT: Cape grid ===
        int leftEnd = w / 2 - 10;
        int leftCx = leftEnd / 2;
        ctx.drawText(this.textRenderer, txt("CAPES", 0xC678DD), leftCx - textW("CAPES") / 2, 40, -1, false);

        int cols = 3, cardW = 56, cardH = 62, gap = 6;
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = leftCx - gridW / 2, startY = 54;

        for (int i = 0; i < CAPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gap), y = startY + row * (cardH + gap);
            boolean hov = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
            boolean sel = i == selectedCape;

            // Card background
            if (sel) {
                drawRoundRect(ctx, x - 1, y - 1, cardW + 2, cardH + 2, 0x22C678DD);
                drawRoundRect(ctx, x, y, cardW, cardH, 0x44C678DD);
                ctx.fill(x + 2, y, x + cardW - 2, y + 1, 0xAAD19A66);
            } else {
                drawRoundRect(ctx, x, y, cardW, cardH, hov ? 0x18C678DD : 0x0CFFFFFF);
                if (hov) drawRoundRectOutline(ctx, x, y, cardW, cardH, 0x22C678DD);
            }

            // Cape preview gradient
            if (i < CAPE_TOP.length - 1) {
                int px = x + 10, py = y + 5, pw = cardW - 20, ph = 28;
                for (int r2 = 0; r2 < ph; r2++) {
                    float t = (float) r2 / (ph - 1);
                    int rv = (int)(CAPE_TOP[i][0]*(1-t) + CAPE_BOT[i][0]*t);
                    int gv = (int)(CAPE_TOP[i][1]*(1-t) + CAPE_BOT[i][1]*t);
                    int bv = (int)(CAPE_TOP[i][2]*(1-t) + CAPE_BOT[i][2]*t);
                    int indent = r2 < 2 ? (2 - r2) : (r2 >= ph - 2 ? (r2 - ph + 3) : 0);
                    ctx.fill(px + indent, py + r2, px + pw - indent, py + r2 + 1, 0xFF000000|(rv<<16)|(gv<<8)|bv);
                }
            } else {
                ctx.drawText(this.textRenderer, txt("OFF", 0x3E4451), x + cardW / 2 - textW("OFF") / 2, y + 16, -1, false);
            }

            // Cape name
            String name = CAPE_NAMES[i];
            int nw = textW(name);
            if (nw > cardW - 4) { name = name.substring(0, Math.min(name.length(), 6)) + ".."; nw = textW(name); }
            int nameColor = sel ? 0xE0E0E8 : (hov ? 0xABB2BF : 0x5C6370);
            ctx.drawText(this.textRenderer, txt(name, nameColor), x + cardW / 2 - nw / 2, y + 37, -1, false);

            // Equipped label
            if (sel && i < CAPE_FILES.length - 1) {
                String eq = "Equipped";
                ctx.drawText(this.textRenderer, txt(eq, 0x98C379), x + cardW / 2 - textW(eq) / 2, y + 49, -1, false);
            }
        }

        // === RIGHT: Preview panel ===
        int previewX = w / 2 + 5, previewW = w / 2 - 15;
        int previewCx = previewX + previewW / 2;

        // Preview background with rounded rect and subtle border
        drawRoundRect(ctx, previewX, 40, previewW, h - 74, 0x0CFFFFFF);
        drawRoundRectOutline(ctx, previewX, 40, previewW, h - 74, 0x0FC070DD);
        ctx.drawText(this.textRenderer, txt("PREVIEW", 0x5C6370), previewCx - textW("PREVIEW") / 2, 44, -1, false);

        // 3D avatar preview
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            if (CosmeticsCape.showCape && CosmeticsCape.capeFile != null) {
                Identifier capeId = Identifier.of("bloom-core", "textures/cape/" + CosmeticsCape.capeFile);
                mc.getTextureManager().registerTexture(capeId, new ResourceTexture(capeId));
            }
            int entitySize = Math.min(previewW / 3, (h - 140));
            int x1 = previewX + 8, y1 = 56, x2 = previewX + previewW - 8, y2 = h - 50;
            drawEntityWithRotation(ctx, x1, y1, x2, y2, entitySize, 0.0625f, playerRotation, mc.player);
        } else {
            ctx.drawText(this.textRenderer, txt("Join a world to preview", 0x3E4451),
                previewCx - textW("Join a world to preview") / 2, h / 2, -1, false);
        }

        // Cape name label below preview
        if (selectedCape < CAPE_FILES.length - 1) {
            String capeName = CAPE_NAMES[selectedCape];
            ctx.drawText(this.textRenderer, txt(capeName, 0xC678DD),
                previewCx - textW(capeName) / 2, h - 64, -1, false);
        }

        // Drag hint
        ctx.drawText(this.textRenderer, txt("Drag to rotate", 0x3E4451),
            previewCx - textW("Drag to rotate") / 2, h - 52, -1, false);

        // Back button
        int backW = 80, backH = 18, bx = cx - backW / 2, by = h - 28;
        boolean bh = mouseX >= bx && mouseX <= bx + backW && mouseY >= by && mouseY <= by + backH;
        drawRoundRect(ctx, bx, by, backW, backH, bh ? 0x33C678DD : 0x10FFFFFF);
        drawRoundRectOutline(ctx, bx, by, backW, backH, bh ? 0x44C678DD : 0x0AFFFFFF);
        ctx.drawText(this.textRenderer, txt("Back", bh ? 0xE0E0E8 : 0x5C6370),
            cx - textW("Back") / 2, by + 5, -1, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mx = click.x(), my = click.y();
        int w = this.width, cx = w / 2;

        // Cape cards
        int leftEnd = w / 2 - 10, cols = 3, cardW = 56, cardH = 62, gap = 6;
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = leftEnd / 2 - gridW / 2, startY = 54;
        for (int i = 0; i < CAPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gap), y = startY + row * (cardH + gap);
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                selectedCape = i;
                if (CAPE_FILES[i] == null) { CosmeticsCape.showCape = false; }
                else {
                    CosmeticsCape.showCape = true; CosmeticsCape.capeFile = CAPE_FILES[i];
                    Identifier id = Identifier.of("bloom-core", "textures/cape/" + CAPE_FILES[i]);
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new ResourceTexture(id));
                }
                syncCosmeticsToFile();
                return true;
            }
        }

        // Back button
        int backW = 80, backH = 18, bx = cx - backW / 2, by = this.height - 28;
        if (mx >= bx && mx <= bx + backW && my >= by && my <= by + backH) { client.setScreen(parent); return true; }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        playerRotation += (float) deltaX * 1.5f;
        return true;
    }

    /** Sync the current cape selection to the shared pulsar-cosmetics.json file */
    private void syncCosmeticsToFile() {
        try {
            // The shared file lives in the pulsar app data dir (parent of game dir)
            Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
            Path bloomDir = gameDir.getParent().getParent(); // profiles/1.21.11 -> profiles -> pulsar
            Path cosmeticsFile = bloomDir.resolve("bloom-cosmetics.json");

            // Read existing data or create default
            String json;
            if (Files.exists(cosmeticsFile)) {
                json = Files.readString(cosmeticsFile);
            } else {
                json = "{\"points\":500,\"owned\":[],\"equipped\":{}}";
            }

            // Simple JSON manipulation (avoid adding a JSON library dependency)
            // Update the "equipped" cape field
            String capeId = "";
            if (CosmeticsCape.showCape && CosmeticsCape.capeFile != null) {
                // Map cape file to cosmetic ID
                String[] ids = {"cape_nebula", "cape_midnight", "cape_frost", "cape_flame",
                    "cape_ocean", "cape_emerald", "cape_sunset", "cape_galaxy"};
                for (int j = 0; j < CAPE_FILES.length - 1; j++) {
                    if (CAPE_FILES[j] != null && CAPE_FILES[j].equals(CosmeticsCape.capeFile)) {
                        capeId = ids[j]; break;
                    }
                }
            }

            // Replace or add the cape in equipped
            if (json.contains("\"equipped\"")) {
                // Replace entire equipped section
                int eqStart = json.indexOf("\"equipped\"");
                int braceStart = json.indexOf("{", eqStart + 10);
                int braceEnd = json.indexOf("}", braceStart) + 1;
                String newEquipped = capeId.isEmpty()
                    ? "\"equipped\":{}"
                    : "\"equipped\":{\"cape\":\"" + capeId + "\"}";
                json = json.substring(0, eqStart) + newEquipped + json.substring(braceEnd);
            }

            Files.writeString(cosmeticsFile, json);
        } catch (Exception ignored) {}
    }

    @Override public boolean shouldCloseOnEsc() { return true; }
    @Override public void close() { client.setScreen(parent); }

    private static void drawEntityWithRotation(DrawContext ctx, int x1, int y1, int x2, int y2,
                                                int size, float yOffset, float rotationDegrees,
                                                LivingEntity entity) {
        Quaternionf flipQuat = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf tiltQuat = new Quaternionf();

        MinecraftClient mc = MinecraftClient.getInstance();
        EntityRenderManager dispatcher = mc.getEntityRenderDispatcher();
        @SuppressWarnings("rawtypes")
        EntityRenderer renderer = dispatcher.getRenderer(entity);
        @SuppressWarnings("unchecked")
        EntityRenderState state = (EntityRenderState) renderer.getAndUpdateRenderState(entity, 1.0f);
        state.light = 15728880;
        state.shadowPieces.clear();
        state.outlineColor = 0;

        if (state instanceof LivingEntityRenderState livingState) {
            livingState.bodyYaw = 180.0f + rotationDegrees;
            livingState.relativeHeadYaw = 0.0f;
            livingState.pitch = 0.0f;
            livingState.width /= livingState.baseScale;
            livingState.height /= livingState.baseScale;
            livingState.baseScale = 1.0f;
        }

        Vector3f offset = new Vector3f(0.0f, state.height / 2.0f + yOffset, 0.0f);
        ctx.addEntity(state, (float) size, offset, flipQuat, tiltQuat, x1, y1, x2, y2);
    }
}
