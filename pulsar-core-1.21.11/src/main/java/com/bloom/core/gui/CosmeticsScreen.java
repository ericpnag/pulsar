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

import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class CosmeticsScreen extends Screen {
    private final Screen parent;
    private int selectedCape = 0;
    private float playerRotation = 0;
    private int scrollOffset = 0;
    private Set<String> purchasedIds = new HashSet<>();

    private static final StyleSpriteSource BLOOM_FONT = new StyleSpriteSource.Font(Identifier.of("pulsar-core", "inter"));

    // Cape IDs must match the Shop.tsx COSMETICS list exactly
    private static final String[] CAPE_IDS = {
        "cape_blossom", "cape_midnight", "cape_frost", "cape_flame",
        "cape_ocean", "cape_emerald", "cape_sunset", "cape_galaxy",
        "cape_void", "cape_lightning", "cape_blood", "cape_arctic",
        "cape_phantom", "cape_neon", "cape_lava", "cape_sakura",
        "cape_storm", "cape_solar", "cape_amethyst", "cape_inferno",
        "cape_drift", "cape_obsidian", "cape_blackhole",
        "cape_creator", "cape_youtube", "cape_twitch", "cape_tiktok", "cape_og"
    };
    private static final String[] CAPE_NAMES = {
        "Bloom", "Midnight", "Frost", "Flame",
        "Ocean", "Emerald", "Sunset", "Galaxy",
        "Void", "Lightning", "Bloodmoon", "Arctic",
        "Phantom", "Neon", "Lava", "Sakura",
        "Storm", "Solar", "Amethyst", "Inferno",
        "Drift", "Obsidian", "Black Hole",
        "Creator", "YouTube", "Twitch", "TikTok", "OG Pulsar", "None"
    };
    private static final String[] CAPE_FILES = {
        "bloom_cape.png", "midnight_cape.png", "frost_cape.png", "flame_cape.png",
        "ocean_cape.png", "emerald_cape.png", "sunset_cape.png", "galaxy_cape.png",
        "void_cape.png", "lightning_cape.png", "blood_cape.png", "arctic_cape.png",
        "phantom_cape.png", "neon_cape.png", "lava_cape.png", "sakura_cape.png",
        "storm_cape.png", "solar_cape.png", "amethyst_cape.png", "inferno_cape.png",
        "drift_cape.png", "obsidian_cape.png", "blackhole_cape.png",
        "creator_cape.png", "youtube_cape.png", "twitch_cape.png", "tiktok_cape.png", "og_cape.png", null
    };
    private static final int[][] CAPE_TOP = {
        {255,190,210}, {30,15,50}, {140,200,255}, {255,120,40},
        {20,80,160}, {30,150,70}, {255,150,80}, {15,8,30},
        {26,10,46}, {232,212,77}, {139,0,0}, {224,240,255},
        {200,200,210}, {15,5,25}, {255,160,20}, {255,200,220},
        {60,65,75}, {255,200,60}, {80,20,120}, {60,5,5},
        {200,180,220}, {25,15,30}, {255,160,50},
        {255,215,0}, {255,0,0}, {145,70,255}, {0,242,234}, {255,255,255}, {40,40,40}
    };
    private static final int[][] CAPE_BOT = {
        {215,120,150}, {15,8,30}, {80,140,220}, {200,60,20},
        {10,40,100}, {15,100,40}, {120,50,150}, {5,2,15},
        {13,5,24}, {184,150,15}, {74,0,0}, {160,196,232},
        {100,100,120}, {5,2,15}, {50,25,5}, {200,140,160},
        {25,28,35}, {200,80,10}, {40,8,60}, {200,30,0},
        {180,200,220}, {10,5,15}, {80,30,5},
        {255,69,0}, {204,0,0}, {100,65,165}, {255,0,80}, {128,128,128}, {30,30,30}
    };

    public CosmeticsScreen(Screen parent) {
        super(Text.literal("Cosmetics"));
        this.parent = parent;
    }

    /** Returns true if the player owns the cape at the given grid index */
    private boolean isOwned(int index) {
        // "None" (last) and "Bloom" (index 0, free) are always available
        if (index == CAPE_NAMES.length - 1) return true;
        if (index == 0) return true; // cape_blossom is free
        if (index < CAPE_IDS.length) return purchasedIds.contains(CAPE_IDS[index]);
        return false;
    }

    /** Read purchased IDs from pulsar-cosmetics.json */
    private Set<String> loadPurchasedIds() {
        Set<String> ids = new HashSet<>();
        try {
            Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
            Path pulsarDir = gameDir.getParent().getParent();
            Path cosmeticsFile = pulsarDir.resolve("pulsar-cosmetics.json");
            if (!Files.exists(cosmeticsFile)) return ids;
            String json = Files.readString(cosmeticsFile);

            // Parse "purchased":["id1","id2",...]
            int start = json.indexOf("\"purchased\"");
            if (start < 0) return ids;
            int arrStart = json.indexOf("[", start);
            int arrEnd = json.indexOf("]", arrStart);
            if (arrStart < 0 || arrEnd < 0) return ids;
            String arr = json.substring(arrStart + 1, arrEnd).trim();
            if (arr.isEmpty()) return ids;
            for (String part : arr.split(",")) {
                String id = part.trim().replace("\"", "");
                if (!id.isEmpty()) ids.add(id);
            }
        } catch (Exception ignored) {}
        return ids;
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

    /** Draw a small padlock icon at (x, y) */
    private void drawLock(DrawContext ctx, int x, int y, int color) {
        // Shackle (top arc)
        ctx.fill(x + 2, y,     x + 6, y + 1, color);
        ctx.fill(x + 1, y + 1, x + 2, y + 4, color);
        ctx.fill(x + 6, y + 1, x + 7, y + 4, color);
        // Body
        ctx.fill(x,     y + 4, x + 8, y + 9, color);
        // Keyhole
        ctx.fill(x + 3, y + 6, x + 5, y + 8, 0x60000000);
    }

    @Override
    protected void init() {
        purchasedIds = loadPurchasedIds();
        if (!CosmeticsCape.showCape) {
            selectedCape = CAPE_NAMES.length - 1;
        } else {
            for (int i = 0; i < CAPE_FILES.length; i++) {
                if (CAPE_FILES[i] != null && CAPE_FILES[i].equals(CosmeticsCape.capeFile)) {
                    selectedCape = i; break;
                }
            }
        }
        // If selected cape is no longer owned (points reset), fall back to Bloom
        if (!isOwned(selectedCape)) {
            selectedCape = 0;
            CosmeticsCape.showCape = true;
            CosmeticsCape.capeFile = CAPE_FILES[0];
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int w = this.width, h = this.height, cx = w / 2;

        // Background
        ctx.fill(0, 0, w, h, 0xF00A0A0F);

        // Title area
        ctx.drawText(this.textRenderer, txt("WARDROBE", 0xFFFFFF), cx - textW("WARDROBE") / 2, 10, -1, false);
        ctx.drawText(this.textRenderer, txt("Customize your look", 0x5C6370), cx - textW("Customize your look") / 2, 22, -1, false);
        ctx.fill(cx - 50, 33, cx + 50, 34, 0x15FFFFFF);

        // === LEFT: Cape grid ===
        int leftEnd = w / 2 - 10;
        int leftCx = leftEnd / 2;

        ctx.drawText(this.textRenderer, txt("CAPES", 0xFFFFFF), leftCx - textW("CAPES") / 2, 40, -1, false);

        int cols = 4, cardW = 42, cardH = 52, gap = 4;
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = leftCx - gridW / 2, startY = 54;

        int totalRows = (CAPE_NAMES.length + cols - 1) / cols;
        int maxScroll = Math.max(0, totalRows * (cardH + gap) - (h - startY - 30));
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        for (int i = 0; i < CAPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gap), y = startY + row * (cardH + gap) - scrollOffset;

            if (y + cardH < 40 || y > h) continue;

            boolean owned = isOwned(i);
            boolean hov = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH && y >= 40;
            boolean sel = i == selectedCape;

            // Card background
            if (sel) {
                drawRoundRect(ctx, x - 1, y - 1, cardW + 2, cardH + 2, 0x22FFFFFF);
                drawRoundRect(ctx, x, y, cardW, cardH, 0x44FFFFFF);
                ctx.fill(x + 2, y, x + cardW - 2, y + 1, 0xAAA0A0A0);
            } else if (!owned) {
                drawRoundRect(ctx, x, y, cardW, cardH, 0x0A000000);
                if (hov) drawRoundRectOutline(ctx, x, y, cardW, cardH, 0x15FFFFFF);
            } else {
                drawRoundRect(ctx, x, y, cardW, cardH, hov ? 0x18FFFFFF : 0x0CFFFFFF);
                if (hov) drawRoundRectOutline(ctx, x, y, cardW, cardH, 0x22FFFFFF);
            }

            // Cape preview gradient
            if (i < CAPE_TOP.length - 1) {
                int px = x + 10, py = y + 5, pw = cardW - 20, ph = 28;
                for (int r2 = 0; r2 < ph; r2++) {
                    float t = (float) r2 / (ph - 1);
                    int rv = (int)(CAPE_TOP[i][0]*(1-t) + CAPE_BOT[i][0]*t);
                    int gv = (int)(CAPE_TOP[i][1]*(1-t) + CAPE_BOT[i][1]*t);
                    int bv = (int)(CAPE_TOP[i][2]*(1-t) + CAPE_BOT[i][2]*t);
                    if (!owned) { rv = rv/4; gv = gv/4; bv = bv/4; } // darken locked
                    int indent = r2 < 2 ? (2 - r2) : (r2 >= ph - 2 ? (r2 - ph + 3) : 0);
                    ctx.fill(px + indent, py + r2, px + pw - indent, py + r2 + 1, 0xFF000000|(rv<<16)|(gv<<8)|bv);
                }
                // Lock icon overlay for unowned capes
                if (!owned) {
                    drawLock(ctx, x + cardW / 2 - 4, y + 8, 0xCCFFFFFF);
                }
            } else {
                ctx.drawText(this.textRenderer, txt("OFF", 0x3E4451), x + cardW / 2 - textW("OFF") / 2, y + 16, -1, false);
            }

            // Cape name
            String name = CAPE_NAMES[i];
            int nw = textW(name);
            if (nw > cardW - 4) { name = name.substring(0, Math.min(name.length(), 6)) + ".."; nw = textW(name); }
            int nameColor = !owned ? 0x3E4451 : (sel ? 0xE0E0E8 : (hov ? 0xABB2BF : 0x5C6370));
            ctx.drawText(this.textRenderer, txt(name, nameColor), x + cardW / 2 - nw / 2, y + 37, -1, false);

            // "Buy" hint on hover for locked capes
            if (!owned && hov) {
                String buy = "Buy";
                ctx.drawText(this.textRenderer, txt(buy, 0x888888), x + cardW / 2 - textW(buy) / 2, y + 49, -1, false);
            }

            // Equipped label
            if (sel && owned && i < CAPE_FILES.length - 1) {
                String eq = "Equipped";
                ctx.drawText(this.textRenderer, txt(eq, 0x98C379), x + cardW / 2 - textW(eq) / 2, y + 49, -1, false);
            }
        }

        // === RIGHT: Preview panel ===
        int previewX = w / 2 + 5, previewW = w / 2 - 15;
        int previewCx = previewX + previewW / 2;

        drawRoundRect(ctx, previewX, 40, previewW, h - 74, 0x0CFFFFFF);
        drawRoundRectOutline(ctx, previewX, 40, previewW, h - 74, 0x0FFFFFFF);
        ctx.drawText(this.textRenderer, txt("PREVIEW", 0x5C6370), previewCx - textW("PREVIEW") / 2, 44, -1, false);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            if (CosmeticsCape.showCape && CosmeticsCape.capeFile != null) {
                Identifier capeId = Identifier.of("pulsar-core", "textures/cape/" + CosmeticsCape.capeFile);
                mc.getTextureManager().registerTexture(capeId, new ResourceTexture(capeId));
            }
            int entitySize = Math.min(previewW / 3, (h - 140));
            int x1 = previewX + 8, y1 = 56, x2 = previewX + previewW - 8, y2 = h - 50;
            drawEntityWithRotation(ctx, x1, y1, x2, y2, entitySize, 0.0625f, playerRotation, mc.player);
        } else {
            ctx.drawText(this.textRenderer, txt("Join a world to preview", 0x3E4451),
                previewCx - textW("Join a world to preview") / 2, h / 2, -1, false);
        }

        if (selectedCape < CAPE_FILES.length - 1) {
            String capeName = CAPE_NAMES[selectedCape];
            ctx.drawText(this.textRenderer, txt(capeName, 0xFFFFFF),
                previewCx - textW(capeName) / 2, h - 64, -1, false);
        }

        ctx.drawText(this.textRenderer, txt("Drag to rotate", 0x3E4451),
            previewCx - textW("Drag to rotate") / 2, h - 52, -1, false);

        // Back button
        int backW = 80, backH = 18, bx = cx - backW / 2, by = h - 28;
        boolean bh = mouseX >= bx && mouseX <= bx + backW && mouseY >= by && mouseY <= by + backH;
        drawRoundRect(ctx, bx, by, backW, backH, bh ? 0x33FFFFFF : 0x10FFFFFF);
        drawRoundRectOutline(ctx, bx, by, backW, backH, bh ? 0x44FFFFFF : 0x0AFFFFFF);
        ctx.drawText(this.textRenderer, txt("Back", bh ? 0xE0E0E8 : 0x5C6370),
            cx - textW("Back") / 2, by + 5, -1, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mx = click.x(), my = click.y();
        int w = this.width, cx = w / 2;

        int leftEnd = w / 2 - 10, cols = 4, cardW = 42, cardH = 52, gap = 4;
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = leftEnd / 2 - gridW / 2, startY = 54;
        for (int i = 0; i < CAPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gap), y = startY + row * (cardH + gap) - scrollOffset;
            if (y < 40 || y > this.height) continue;
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                if (!isOwned(i)) return true; // blocked — not purchased
                selectedCape = i;
                if (CAPE_FILES[i] == null) { CosmeticsCape.showCape = false; }
                else {
                    CosmeticsCape.showCape = true; CosmeticsCape.capeFile = CAPE_FILES[i];
                    Identifier id = Identifier.of("pulsar-core", "textures/cape/" + CAPE_FILES[i]);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < this.width / 2) {
            scrollOffset -= (int)(verticalAmount * 20);
            if (scrollOffset < 0) scrollOffset = 0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        playerRotation += (float) deltaX * 1.5f;
        return true;
    }

    /** Sync current cape selection to pulsar-cosmetics.json (v2 format) */
    private void syncCosmeticsToFile() {
        try {
            Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
            Path pulsarDir = gameDir.getParent().getParent();
            Path cosmeticsFile = pulsarDir.resolve("pulsar-cosmetics.json");

            String json;
            if (Files.exists(cosmeticsFile)) {
                json = Files.readString(cosmeticsFile);
            } else {
                json = "{\"v\":2,\"points\":500,\"purchased\":[],\"equipped\":{}}";
            }

            String capeId = "";
            if (CosmeticsCape.showCape && CosmeticsCape.capeFile != null) {
                for (int j = 0; j < CAPE_FILES.length - 1; j++) {
                    if (CAPE_FILES[j] != null && CAPE_FILES[j].equals(CosmeticsCape.capeFile)) {
                        capeId = CAPE_IDS[j]; break;
                    }
                }
            }

            if (json.contains("\"equipped\"")) {
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
