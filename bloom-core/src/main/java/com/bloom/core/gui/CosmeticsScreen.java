package com.bloom.core.gui;

import com.bloom.core.module.modules.CosmeticsCape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CosmeticsScreen extends Screen {
    private final Screen parent;
    private int selectedCape = 0;
    private float playerRotation = 155f;
    private boolean draggingPlayer = false;
    private double lastMouseX = 0;

    private static final String[] CAPE_NAMES = {
        "Cherry Blossom", "Midnight", "Frost", "Flame",
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

    @Override
    protected void init() {
        if (!CosmeticsCape.showCape) {
            selectedCape = CAPE_NAMES.length - 1;
        } else {
            for (int i = 0; i < CAPE_FILES.length; i++) {
                if (CAPE_FILES[i] != null && CAPE_FILES[i].equals(CosmeticsCape.capeFile)) { selectedCape = i; break; }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        ctx.fill(0, 0, w, h, 0xEE0a0611);

        if (draggingPlayer) { playerRotation += (float)(mouseX - lastMouseX) * 2f; }
        lastMouseX = mouseX;

        String title = "COSMETICS";
        ctx.drawText(this.textRenderer, title, cx - this.textRenderer.getWidth(title)/2, 8, 0xFFFFD1DC, false);
        ctx.fill(cx - 40, 20, cx + 40, 21, 0x22FFB7C9);

        int leftEnd = w / 2 - 5;
        ctx.drawText(this.textRenderer, "CAPES", leftEnd/2 - this.textRenderer.getWidth("CAPES")/2, 26, 0xFFFFB7C9, false);

        int cols = 3, cardW = 50, cardH = 56, gap = 4;
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = leftEnd/2 - gridW/2, startY = 38;

        for (int i = 0; i < CAPE_NAMES.length; i++) {
            int col = i%cols, row = i/cols;
            int x = startX + col*(cardW+gap), y = startY + row*(cardH+gap);
            boolean hov = mouseX>=x && mouseX<=x+cardW && mouseY>=y && mouseY<=y+cardH;
            boolean sel = i == selectedCape;

            ctx.fill(x, y, x+cardW, y+cardH, sel ? 0x44FFB7C9 : (hov ? 0x28FFB7C9 : 0x15FFFFFF));
            if (sel) {
                ctx.fill(x, y, x+cardW, y+1, 0xAAFFB7C9);
                ctx.fill(x, y+cardH-1, x+cardW, y+cardH, 0xAAFFB7C9);
                ctx.fill(x, y, x+1, y+cardH, 0xAAFFB7C9);
                ctx.fill(x+cardW-1, y, x+cardW, y+cardH, 0xAAFFB7C9);
            }

            if (i < CAPE_TOP.length - 1) {
                int px = x+12, py = y+4, pw = 26, ph = 28;
                for (int r2 = 0; r2 < ph; r2++) {
                    float t = (float)r2/(ph-1);
                    int rv = (int)(CAPE_TOP[i][0]*(1-t)+CAPE_BOT[i][0]*t);
                    int gv = (int)(CAPE_TOP[i][1]*(1-t)+CAPE_BOT[i][1]*t);
                    int bv = (int)(CAPE_TOP[i][2]*(1-t)+CAPE_BOT[i][2]*t);
                    int indent = Math.max(0, 3-r2/5);
                    ctx.fill(px+indent, py+r2, px+pw-indent, py+r2+1, 0xFF000000|(rv<<16)|(gv<<8)|bv);
                }
            } else {
                ctx.drawText(this.textRenderer, "OFF", x+cardW/2-this.textRenderer.getWidth("OFF")/2, y+14, 0xFF5A4550, false);
            }

            String name = CAPE_NAMES[i]; int nw = this.textRenderer.getWidth(name);
            if (nw > cardW-4) { name=name.substring(0,Math.min(name.length(),6))+".."; nw=this.textRenderer.getWidth(name); }
            ctx.drawText(this.textRenderer, name, x+cardW/2-nw/2, y+35, hov||sel ? 0xFFF0E4E8 : 0xFF8A7080, false);
            if (sel) ctx.drawText(this.textRenderer, "Equipped", x+cardW/2-this.textRenderer.getWidth("Equipped")/2, y+46, 0xFF6EE7A0, false);
        }

        // === RIGHT: Preview ===
        int previewX = w/2+5, previewW = w/2-15, previewCx = previewX+previewW/2;
        ctx.fill(previewX, 26, previewX+previewW, h-30, 0x18FFFFFF);
        ctx.fill(previewX, 26, previewX+previewW, 27, 0x22FFB7C9);
        ctx.drawText(this.textRenderer, "PREVIEW", previewCx-this.textRenderer.getWidth("PREVIEW")/2, 30, 0xFF8A7080, false);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            if (CosmeticsCape.showCape && CosmeticsCape.capeFile != null) {
                Identifier capeId = Identifier.of("bloom-core", "textures/cape/" + CosmeticsCape.capeFile);
                mc.getTextureManager().registerTexture(capeId, new ResourceTexture(capeId));
            }
            int entitySize = Math.min(previewW/3, h-100);
            float oy=mc.player.getYaw(), ob=mc.player.bodyYaw, oh=mc.player.headYaw, op=mc.player.getPitch();
            float opy=mc.player.prevYaw, opb=mc.player.prevBodyYaw, oph=mc.player.prevHeadYaw, opp=mc.player.prevPitch;
            mc.player.setYaw(playerRotation); mc.player.bodyYaw=playerRotation; mc.player.headYaw=playerRotation;
            mc.player.prevYaw=playerRotation; mc.player.prevBodyYaw=playerRotation; mc.player.prevHeadYaw=playerRotation;
            mc.player.setPitch(0); mc.player.prevPitch=0;
            InventoryScreen.drawEntity(ctx, previewX+8, 42, previewX+previewW-8, h-35, entitySize, 0.1f, previewCx, h/2-20, mc.player);
            mc.player.setYaw(oy); mc.player.bodyYaw=ob; mc.player.headYaw=oh; mc.player.setPitch(op);
            mc.player.prevYaw=opy; mc.player.prevBodyYaw=opb; mc.player.prevHeadYaw=oph; mc.player.prevPitch=opp;
        } else {
            int charX = previewCx, charY = h/2+10, s = 4;

            Identifier skinTex = null;
            try {
                java.util.UUID uuid = mc.getSession().getUuidOrNull();
                if (uuid != null) {
                    com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(uuid, mc.getSession().getUsername());
                    var skinTextures = mc.getSkinProvider().getSkinTextures(profile);
                    if (skinTextures != null && skinTextures.texture() != null) {
                        skinTex = skinTextures.texture();
                    }
                }
            } catch (Exception ignored) {}

            boolean showBack = ((int)((playerRotation%360+360)%360) > 90 && (int)((playerRotation%360+360)%360) < 270);

            if (showBack && selectedCape < CAPE_FILES.length - 1) {
                int capeW=10*s, capeH=16*s, cX=charX-capeW/2, cY=charY-28*s;
                for (int row=0; row<capeH; row++) {
                    float t=(float)row/(capeH-1);
                    int rv=(int)(CAPE_TOP[selectedCape][0]*(1-t)+CAPE_BOT[selectedCape][0]*t);
                    int gv=(int)(CAPE_TOP[selectedCape][1]*(1-t)+CAPE_BOT[selectedCape][1]*t);
                    int bv=(int)(CAPE_TOP[selectedCape][2]*(1-t)+CAPE_BOT[selectedCape][2]*t);
                    int widen=row/(s*3);
                    ctx.fill(cX-widen, cY+row, cX+capeW+widen, cY+row+1, 0xFF000000|(rv<<16)|(gv<<8)|bv);
                }
            }

            if (skinTex != null) {
                int headU = showBack ? 24 : 8, headV = 8;
                int bodyU = showBack ? 32 : 20, bodyV = 20;
                int rArmU = showBack ? 52 : 44, rArmV = 20;
                int lArmU = showBack ? 44 : 36, lArmV = 52;
                int rLegU = showBack ? 12 : 4, rLegV = 20;
                int lLegU = showBack ? 28 : 20, lLegV = 52;
                ctx.drawTexture(skinTex, charX-4*s, charY-32*s, 8*s, 8*s, headU, headV, 8, 8, 64, 64);
                ctx.drawTexture(skinTex, charX-4*s, charY-24*s, 8*s, 12*s, bodyU, bodyV, 8, 12, 64, 64);
                ctx.drawTexture(skinTex, charX-8*s, charY-24*s, 4*s, 12*s, rArmU, rArmV, 4, 12, 64, 64);
                ctx.drawTexture(skinTex, charX+4*s, charY-24*s, 4*s, 12*s, lArmU, lArmV, 4, 12, 64, 64);
                ctx.drawTexture(skinTex, charX-4*s, charY-12*s, 4*s, 12*s, rLegU, rLegV, 4, 12, 64, 64);
                ctx.drawTexture(skinTex, charX, charY-12*s, 4*s, 12*s, lLegU, lLegV, 4, 12, 64, 64);
            } else {
                ctx.fill(charX-4*s, charY-32*s, charX+4*s, charY-24*s, 0xFFD4A574);
                ctx.fill(charX-4*s, charY-24*s, charX+4*s, charY-12*s, 0xFF3B3B3B);
                ctx.fill(charX-8*s, charY-24*s, charX-4*s, charY-12*s, 0xFF3B3B3B);
                ctx.fill(charX+4*s, charY-24*s, charX+8*s, charY-12*s, 0xFF3B3B3B);
                ctx.fill(charX-4*s, charY-12*s, charX, charY, 0xFF2A2A2A);
                ctx.fill(charX, charY-12*s, charX+4*s, charY, 0xFF2A2A2A);
            }

            String hint = showBack ? "Cape view" : "Front view";
            ctx.drawText(this.textRenderer, hint, previewCx-this.textRenderer.getWidth(hint)/2, charY+4, 0xFF5A4550, false);
        }

        String drag = mc.player != null ? "Drag to rotate" : "Click to rotate";
        ctx.drawText(this.textRenderer, drag, previewCx-this.textRenderer.getWidth(drag)/2, h-40, 0xFF5A4550, false);

        int backW=60, backH=14, bx=cx-backW/2, by=h-22;
        boolean bh2 = mouseX>=bx && mouseX<=bx+backW && mouseY>=by && mouseY<=by+backH;
        ctx.fill(bx, by, bx+backW, by+backH, bh2 ? 0x33FFB7C9 : 0x18FFFFFF);
        ctx.drawText(this.textRenderer, "Back", cx-this.textRenderer.getWidth("Back")/2, by+3, bh2 ? 0xFFF0E4E8 : 0xFF8A7080, false);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int w = this.width, cx = w / 2;

        int previewX = w/2+5, previewW = w/2-15;
        if (mouseX >= previewX && mouseX <= previewX+previewW && mouseY >= 26 && mouseY <= this.height-30) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) { draggingPlayer = true; }
            else { playerRotation += 180; }
            return true;
        }

        int leftEnd = w/2-5, cols=3, cardW=50, cardH=56, gap=4;
        int gridW = cols*cardW+(cols-1)*gap, startX = leftEnd/2-gridW/2, startY = 38;
        for (int i = 0; i < CAPE_NAMES.length; i++) {
            int col=i%cols, row=i/cols, x=startX+col*(cardW+gap), y=startY+row*(cardH+gap);
            if (mouseX>=x && mouseX<=x+cardW && mouseY>=y && mouseY<=y+cardH) {
                selectedCape = i;
                if (CAPE_FILES[i]==null) { CosmeticsCape.showCape=false; }
                else { CosmeticsCape.showCape=true; CosmeticsCape.capeFile=CAPE_FILES[i];
                    Identifier id = Identifier.of("bloom-core","textures/cape/"+CAPE_FILES[i]);
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new ResourceTexture(id)); }
                return true;
            }
        }
        int backW=60, backH=14, bx=cx-backW/2, by=this.height-22;
        if (mouseX>=bx && mouseX<=bx+backW && mouseY>=by && mouseY<=by+backH) { client.setScreen(parent); return true; }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingPlayer) { playerRotation += (float)deltaX * 2f; return true; }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) { draggingPlayer=false; return super.mouseReleased(mouseX, mouseY, button); }
    @Override public boolean shouldCloseOnEsc() { return true; }
    @Override public void close() { client.setScreen(parent); }
}
