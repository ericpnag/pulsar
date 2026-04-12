package com.bloom.core.cape;

import net.minecraft.client.texture.NativeImage;

/**
 * Generates detailed cape textures at 8x resolution (512x256).
 * Cape UV layout scaled 8x:
 *   Front face: x:8-87, y:8-135 (80x128)
 *   Back face:  x:96-175, y:8-135 (80x128)
 *   Left edge:  x:0-7, y:8-135
 *   Right edge: x:88-95, y:8-135
 *   Top strip:  x:8-87, y:0-7
 */
public class CapeTextureGenerator {

    private static final int SCALE = 8;
    private static final int FW = 10 * SCALE;   // 80
    private static final int FH = 16 * SCALE;   // 128
    private static final int FX = 1 * SCALE;    // 8
    private static final int FY = 1 * SCALE;    // 8
    private static final int BX = 12 * SCALE;   // 96
    private static final int BY = 1 * SCALE;    // 8
    private static final int DEPTH = 1 * SCALE; // 8

    public static int faceWidth() { return FW; }
    public static int faceHeight() { return FH; }

    public static void generateBase(NativeImage img, String capeFile) {
        switch (capeFile) {
            case "bloom_cape.png" -> generateCherryBlossom(img);
            case "midnight_cape.png" -> generateMidnight(img);
            case "frost_cape.png" -> generateFrost(img);
            case "flame_cape.png" -> generateFlame(img);
            case "ocean_cape.png" -> generateOcean(img);
            case "emerald_cape.png" -> generateEmerald(img);
            case "sunset_cape.png" -> generateSunset(img);
            case "galaxy_cape.png" -> generateGalaxy(img);
        }
    }

    // Write to front face and back face
    static void fillFace(NativeImage img, int lx, int ly, int argb) {
        if (lx < 0 || lx >= FW || ly < 0 || ly >= FH) return;
        img.setColorArgb(FX + lx, FY + ly, argb);
        img.setColorArgb(BX + lx, BY + ly, argb);
    }

    static void blendFace(NativeImage img, int lx, int ly, int color, float alpha) {
        if (lx < 0 || lx >= FW || ly < 0 || ly >= FH) return;
        blend(img, FX + lx, FY + ly, color, alpha);
        blend(img, BX + lx, BY + ly, color, alpha);
    }

    private static void fillEdges(NativeImage img, int ly, int argb) {
        if (ly < 0 || ly >= FH) return;
        for (int d = 0; d < DEPTH; d++) {
            img.setColorArgb(d, FY + ly, argb);
            img.setColorArgb(FX + FW + d, FY + ly, argb);
        }
    }

    private static void fillTop(NativeImage img, int lx, int argb) {
        if (lx < 0 || lx >= FW) return;
        for (int d = 0; d < DEPTH; d++) {
            img.setColorArgb(FX + lx, d, argb);
        }
    }

    // ========== PERLIN-ISH NOISE HELPER ==========
    private static float noise2D(float x, float y) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        float fx = x - ix, fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        float a = pseudoRand(ix, iy), b = pseudoRand(ix + 1, iy);
        float c = pseudoRand(ix, iy + 1), d = pseudoRand(ix + 1, iy + 1);
        return lerpF(lerpF(a, b, fx), lerpF(c, d, fx), fy);
    }

    private static float pseudoRand(int x, int y) {
        int n = x + y * 57;
        n = (n << 13) ^ n;
        return (1.0f - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f) * 0.5f + 0.5f;
    }

    private static float fbm(float x, float y, int octaves) {
        float value = 0, amp = 0.5f, freq = 1;
        for (int i = 0; i < octaves; i++) {
            value += noise2D(x * freq, y * freq) * amp;
            amp *= 0.5f;
            freq *= 2;
        }
        return value;
    }

    // ========== CHERRY BLOSSOM ==========
    private static void generateCherryBlossom(NativeImage img) {
        // Rich pink gradient background with subtle texture
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            for (int x = 0; x < FW; x++) {
                float n = fbm(x * 0.05f, y * 0.05f, 3) * 0.08f;
                float tt = Math.max(0, Math.min(1, t + n));
                int r = lerp(248, 195, tt), g = lerp(185, 110, tt), b = lerp(205, 140, tt);
                fillFace(img, x, y, argb(255, r, g, b));
            }
            int ec = argb(255, lerp(245, 195, t), lerp(178, 110, t), lerp(200, 140, t));
            fillEdges(img, y, ec);
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 248, 185, 205));

        // Branch with bark texture (diagonal across cape)
        drawBranch(img, -5, 45, 85, 25, 3.0f);
        drawBranch(img, 20, 100, 65, 70, 2.5f);
        // Smaller twigs
        drawBranch(img, 30, 30, 50, 15, 1.5f);
        drawBranch(img, 55, 75, 75, 55, 1.5f);

        // Large detailed cherry blossoms at key positions
        drawFlower(img, 16, 20, 18, 0.0f);
        drawFlower(img, 56, 12, 20, 0.4f);
        drawFlower(img, 34, 55, 19, 0.7f);
        drawFlower(img, 64, 70, 16, 0.2f);
        drawFlower(img, 12, 90, 17, 0.9f);
        drawFlower(img, 50, 105, 15, 0.5f);
        drawFlower(img, 70, 40, 13, 0.1f);

        // Medium flowers
        drawFlower(img, 8, 50, 11, 0.3f);
        drawFlower(img, 40, 30, 12, 0.6f);
        drawFlower(img, 72, 95, 10, 0.8f);
        drawFlower(img, 25, 80, 11, 0.15f);

        // Small buds scattered
        drawBud(img, 5, 10, 5);
        drawBud(img, 40, 8, 4);
        drawBud(img, 75, 25, 5);
        drawBud(img, 6, 70, 4);
        drawBud(img, 60, 45, 5);
        drawBud(img, 20, 115, 4);
        drawBud(img, 68, 115, 5);
        drawBud(img, 45, 120, 3);

        // Falling petal shapes (static base for animation to move)
        for (int i = 0; i < 12; i++) {
            int px = (int)(pseudoRand(i * 7, 42) * FW);
            int py = (int)(pseudoRand(i * 13, 17) * FH);
            float rot = pseudoRand(i * 3, 99) * 6.28f;
            drawPetal(img, px, py, 4 + (int)(pseudoRand(i, 5) * 3), rot, 0.4f);
        }

        // Soft vignette darkening at edges
        applyVignette(img, argb(255, 160, 80, 100), 0.15f);
    }

    private static void drawBranch(NativeImage img, int x0, int y0, int x1, int y1, float thickness) {
        int barkDark = argb(255, 95, 60, 40);
        int barkLight = argb(255, 130, 85, 55);
        float dx = x1 - x0, dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        int steps = (int) len;
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            int px = (int)(x0 + dx * t);
            int py = (int)(y0 + dy * t);
            float w = thickness * (1.0f - t * 0.4f);
            for (int oy = (int)-w - 1; oy <= (int) w + 1; oy++) {
                for (int ox = (int)-w - 1; ox <= (int) w + 1; ox++) {
                    float d = (float) Math.sqrt(ox * ox + oy * oy);
                    if (d <= w) {
                        float edge = d / w;
                        int c = edge > 0.6f ? barkDark : barkLight;
                        blendFace(img, px + ox, py + oy, c, 0.85f * (1 - edge * 0.3f));
                    }
                }
            }
        }
    }

    private static void drawFlower(NativeImage img, int cx, int cy, int size, float rotOffset) {
        // Petal colors with more variation
        int petalLight = argb(255, 255, 220, 232);
        int petalMid = argb(255, 252, 198, 215);
        int petalDark = argb(255, 242, 175, 198);
        int petalDeep = argb(255, 228, 150, 178);
        int centerColor = argb(255, 255, 248, 210);
        int stamenColor = argb(255, 200, 135, 105);
        int stamenTip = argb(255, 220, 160, 60);

        // 5 petals with rounded heart-like shape
        for (int p = 0; p < 5; p++) {
            double angle = rotOffset + p * Math.PI * 2.0 / 5.0 - Math.PI / 2.0;
            float pcx = cx + (float)(Math.cos(angle) * size * 0.40);
            float pcy = cy + (float)(Math.sin(angle) * size * 0.40);
            float pLen = size * 0.58f;
            float pWid = size * 0.38f;

            for (int py = (int)(pcy - pLen - 2); py <= (int)(pcy + pLen + 2); py++) {
                for (int px = (int)(pcx - pLen - 2); px <= (int)(pcx + pLen + 2); px++) {
                    float ddx = px - pcx, ddy = py - pcy;
                    float cos = (float) Math.cos(-angle), sin = (float) Math.sin(-angle);
                    float lx = ddx * cos - ddy * sin;
                    float ly = ddx * sin + ddy * cos;
                    float ex = lx / pLen, ey = ly / pWid;
                    float dist = ex * ex + ey * ey;

                    if (dist <= 1.0f) {
                        // Petal notch at tip (heart shape)
                        if (ex > 0.55f && Math.abs(ey) < 0.10f) continue;

                        float fromCenter = (float) Math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy)) / size;
                        // Gradient from center outward with vein lines
                        float vein = (float)(Math.sin(lx * 2.5f) * 0.5 + 0.5) * 0.15f;
                        int color;
                        if (dist > 0.85f) color = petalDeep;
                        else if (dist > 0.6f) color = petalDark;
                        else if (fromCenter < 0.25f) color = petalLight;
                        else color = petalMid;

                        float alpha = dist > 0.9f ? 0.7f : 0.92f;
                        blendFace(img, px, py, color, alpha);
                        // Subtle vein overlay
                        if (vein > 0.1f && dist < 0.8f) {
                            blendFace(img, px, py, petalDeep, vein * 0.3f);
                        }
                    }
                }
            }
        }

        // Center cluster
        int cRad = Math.max(3, size / 4);
        for (int dy = -cRad; dy <= cRad; dy++) {
            for (int dx = -cRad; dx <= cRad; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d <= cRad) {
                    float edge = d / cRad;
                    int c = edge > 0.6f ? argb(255, 245, 235, 195) : centerColor;
                    fillFace(img, cx + dx, cy + dy, c);
                }
            }
        }

        // Stamens radiating outward
        for (int s = 0; s < 7; s++) {
            double sa = rotOffset + s * Math.PI * 2.0 / 7.0 + Math.PI / 7.0;
            for (int i = cRad; i < cRad + size / 3; i++) {
                int sx = cx + (int)(Math.cos(sa) * i);
                int sy = cy + (int)(Math.sin(sa) * i);
                float t = (float)(i - cRad) / (size / 3.0f);
                blendFace(img, sx, sy, stamenColor, 0.7f * (1 - t * 0.5f));
            }
            // Stamen tip (pollen dot)
            int tipX = cx + (int)(Math.cos(sa) * (cRad + size / 3));
            int tipY = cy + (int)(Math.sin(sa) * (cRad + size / 3));
            fillFace(img, tipX, tipY, stamenTip);
            fillFace(img, tipX + 1, tipY, stamenTip);
            fillFace(img, tipX, tipY + 1, stamenTip);
        }
    }

    private static void drawBud(NativeImage img, int cx, int cy, int size) {
        // Teardrop bud shape
        for (int dy = -size; dy <= size + 2; dy++) {
            float w = dy < 0 ? size * (1 - (float) Math.abs(dy) / size) : size * (1 - (float) dy / (size + 2));
            for (int dx = (int)-w; dx <= (int) w; dx++) {
                float d = (float) Math.abs(dx) / Math.max(1, w);
                int color = dy < -size / 2 ? argb(255, 255, 208, 220) :
                    dy < 0 ? argb(255, 248, 190, 210) :
                        argb(255, 235, 165, 185);
                blendFace(img, cx + dx, cy + dy, color, 0.8f * (1 - d * 0.3f));
            }
        }
        // Calyx (green base)
        for (int dx = -2; dx <= 2; dx++) {
            blendFace(img, cx + dx, cy + size + 1, argb(255, 100, 155, 80), 0.7f);
            blendFace(img, cx + dx, cy + size + 2, argb(255, 85, 140, 65), 0.5f);
        }
    }

    private static void drawPetal(NativeImage img, int cx, int cy, int size, float rot, float alpha) {
        // Single loose petal shape
        int color = argb(255, 255, 210, 225);
        for (int dy = -size; dy <= size; dy++) {
            for (int dx = -size; dx <= size; dx++) {
                float cos = (float) Math.cos(-rot), sin = (float) Math.sin(-rot);
                float lx = dx * cos - dy * sin;
                float ly = dx * sin + dy * cos;
                float ex = lx / size, ey = ly / (size * 0.55f);
                float dist = ex * ex + ey * ey;
                if (dist <= 1.0f && dist > 0.1f) {
                    blendFace(img, cx + dx, cy + dy, color, alpha * (1 - dist * 0.5f));
                }
            }
        }
    }

    // ========== MIDNIGHT ==========
    private static void generateMidnight(NativeImage img) {
        // Deep night sky gradient with noise texture
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            for (int x = 0; x < FW; x++) {
                float n = fbm(x * 0.04f, y * 0.04f, 3) * 0.06f;
                float tt = Math.max(0, Math.min(1, t + n));
                int r = lerp(22, 5, tt), g = lerp(14, 2, tt), b = lerp(48, 14, tt);
                fillFace(img, x, y, argb(255, r, g, b));
            }
            fillEdges(img, y, argb(255, lerp(22, 5, t), lerp(14, 2, t), lerp(48, 14, t)));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 22, 14, 48));

        // Large crescent moon with glow halo
        int moonCx = 58, moonCy = 20, moonR = 14;
        // Outer glow
        for (int dy = -moonR - 8; dy <= moonR + 8; dy++) {
            for (int dx = -moonR - 8; dx <= moonR + 8; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > moonR && d < moonR + 8) {
                    float glow = 1.0f - (d - moonR) / 8.0f;
                    blendFace(img, moonCx + dx, moonCy + dy, argb(255, 200, 195, 150), glow * 0.2f);
                }
            }
        }
        // Moon body (crescent)
        for (int dy = -moonR; dy <= moonR; dy++) {
            for (int dx = -moonR; dx <= moonR; dx++) {
                float d1 = (float) Math.sqrt(dx * dx + dy * dy);
                float d2 = (float) Math.sqrt((dx - 5) * (dx - 5) + (dy - 2) * (dy - 2));
                if (d1 <= moonR && d2 > moonR - 2.5f) {
                    float bright = 1.0f - d1 / moonR * 0.2f;
                    // Surface texture
                    float tex = fbm(dx * 0.15f, dy * 0.15f, 2) * 0.08f;
                    bright = Math.max(0.6f, bright - tex);
                    fillFace(img, moonCx + dx, moonCy + dy,
                        argb(255, (int)(255 * bright), (int)(250 * bright), (int)(205 * bright)));
                }
            }
        }

        // 4-point stars with cross-flare effect (larger at 8x)
        drawCrossFlare(img, 12, 12, 6, argb(255, 225, 225, 255));
        drawCrossFlare(img, 36, 28, 5, argb(255, 210, 210, 248));
        drawCrossFlare(img, 68, 50, 7, argb(255, 235, 225, 255));
        drawCrossFlare(img, 16, 78, 5, argb(255, 215, 215, 252));
        drawCrossFlare(img, 52, 100, 6, argb(255, 230, 230, 255));
        drawCrossFlare(img, 72, 110, 4, argb(255, 205, 205, 245));

        // Medium stars (simple 4-point)
        drawStar(img, 30, 8, 4, argb(255, 200, 200, 240));
        drawStar(img, 48, 18, 3, argb(255, 195, 195, 235));
        drawStar(img, 8, 42, 4, argb(255, 210, 210, 248));
        drawStar(img, 44, 60, 3, argb(255, 200, 200, 240));
        drawStar(img, 24, 95, 4, argb(255, 215, 215, 250));

        // Small dot stars scattered
        int[][] smalls = {
            {5, 5}, {20, 4}, {42, 6}, {75, 8}, {8, 25}, {50, 30}, {70, 35},
            {4, 40}, {25, 48}, {60, 42}, {35, 55}, {70, 58}, {10, 65},
            {45, 72}, {65, 75}, {20, 85}, {55, 88}, {75, 92}, {8, 100},
            {35, 105}, {62, 108}, {15, 115}, {48, 118}, {72, 120},
            {30, 122}, {58, 125}
        };
        for (int[] s : smalls) {
            float bright = pseudoRand(s[0], s[1]);
            int c = argb(255, 150 + (int)(bright * 80), 150 + (int)(bright * 70), 200 + (int)(bright * 55));
            fillFace(img, s[0], s[1], c);
            // Tiny glow around brighter ones
            if (bright > 0.6f) {
                blendFace(img, s[0] + 1, s[1], c, 0.3f);
                blendFace(img, s[0] - 1, s[1], c, 0.3f);
                blendFace(img, s[0], s[1] + 1, c, 0.3f);
                blendFace(img, s[0], s[1] - 1, c, 0.3f);
            }
        }

        // Aurora band at bottom with multiple color layers
        for (int y = FH - 24; y < FH; y++) {
            float s = (float)(y - (FH - 24)) / 24.0f;
            for (int x = 0; x < FW; x++) {
                float wave1 = (float)(Math.sin(x * 0.12 + y * 0.06) * 0.5 + 0.5);
                float wave2 = (float)(Math.sin(x * 0.08 - y * 0.04 + 2) * 0.5 + 0.5);
                // Purple-green aurora
                blendFace(img, x, y, argb(255, 70, 20, 110), s * wave1 * 0.3f);
                blendFace(img, x, y, argb(255, 30, 100, 80), s * wave2 * 0.15f);
            }
        }
    }

    private static void drawCrossFlare(NativeImage img, int cx, int cy, int size, int color) {
        // Bright center
        fillFace(img, cx, cy, color);
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        // Diagonal flares (X shape)
        for (int i = 1; i <= size; i++) {
            float fade = 1.0f - (float) i / (size + 1);
            int dim = argb((int)(255 * fade), r, g, b);
            // Cardinal
            blendFace(img, cx + i, cy, dim, fade * 0.9f);
            blendFace(img, cx - i, cy, dim, fade * 0.9f);
            blendFace(img, cx, cy + i, dim, fade * 0.9f);
            blendFace(img, cx, cy - i, dim, fade * 0.9f);
            // Diagonal (shorter, dimmer)
            if (i <= size * 2 / 3) {
                float dfade = fade * 0.4f;
                blendFace(img, cx + i, cy + i, dim, dfade);
                blendFace(img, cx - i, cy + i, dim, dfade);
                blendFace(img, cx + i, cy - i, dim, dfade);
                blendFace(img, cx - i, cy - i, dim, dfade);
            }
        }
        // Core glow (3x3 bright center)
        for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++)
                if (dx != 0 || dy != 0)
                    blendFace(img, cx + dx, cy + dy, color, 0.5f);
    }

    private static void drawStar(NativeImage img, int cx, int cy, int size, int color) {
        fillFace(img, cx, cy, color);
        int dim = argb(180, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        for (int i = 1; i <= size; i++) {
            float fade = 1.0f - (float) i / (size + 1);
            blendFace(img, cx + i, cy, dim, fade);
            blendFace(img, cx - i, cy, dim, fade);
            blendFace(img, cx, cy + i, dim, fade);
            blendFace(img, cx, cy - i, dim, fade);
        }
    }

    // ========== FROST ==========
    private static void generateFrost(NativeImage img) {
        // Ice-blue gradient with crystalline noise
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            for (int x = 0; x < FW; x++) {
                float n = fbm(x * 0.06f, y * 0.06f, 3) * 0.08f;
                float tt = Math.max(0, Math.min(1, t + n));
                int r = lerp(165, 85, tt), g = lerp(218, 170, tt), b = lerp(255, 238, tt);
                fillFace(img, x, y, argb(255, r, g, b));
            }
            fillEdges(img, y, argb(255, lerp(165, 85, t), lerp(218, 170, t), lerp(255, 238, t)));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 165, 218, 255));

        // Frost creep pattern (Voronoi-like crystal boundaries)
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                float v = voronoi(x * 0.08f, y * 0.08f);
                if (v < 0.12f) {
                    blendFace(img, x, y, argb(255, 230, 245, 255), 0.6f * (1 - v / 0.12f));
                }
            }
        }

        // Large detailed snowflakes
        drawSnowflake(img, 20, 22, 16);
        drawSnowflake(img, 58, 16, 18);
        drawSnowflake(img, 38, 60, 14);
        drawSnowflake(img, 14, 95, 12);
        drawSnowflake(img, 65, 85, 16);
        drawSnowflake(img, 40, 110, 10);

        // Ice crystal clusters (small hexagonal formations)
        drawIceCrystal(img, 8, 50, 6);
        drawIceCrystal(img, 72, 40, 5);
        drawIceCrystal(img, 30, 85, 5);
        drawIceCrystal(img, 55, 115, 4);

        // Frost edge glow (neon blue at borders)
        for (int y = 0; y < FH; y++) {
            float edgeDist = Math.min(y, FH - 1 - y) / (float) FH;
            float xEdge = Math.min(0, 0) / (float) FW; // top/bottom edges
            if (edgeDist < 0.06f) {
                float glow = (1 - edgeDist / 0.06f) * 0.25f;
                for (int x = 0; x < FW; x++) {
                    blendFace(img, x, y, argb(255, 200, 240, 255), glow);
                }
            }
        }
        for (int x = 0; x < FW; x++) {
            float edgeDist = Math.min(x, FW - 1 - x) / (float) FW;
            if (edgeDist < 0.06f) {
                float glow = (1 - edgeDist / 0.06f) * 0.2f;
                for (int y = 0; y < FH; y++) {
                    blendFace(img, x, y, argb(255, 200, 240, 255), glow);
                }
            }
        }
    }

    private static float voronoi(float x, float y) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        float minDist = 10;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                float px = ix + dx + pseudoRand(ix + dx, iy + dy);
                float py = iy + dy + pseudoRand(iy + dy + 50, ix + dx + 50);
                float d = (float) Math.sqrt((x - px) * (x - px) + (y - py) * (y - py));
                if (d < minDist) minDist = d;
            }
        }
        return minDist;
    }

    private static void drawSnowflake(NativeImage img, int cx, int cy, int size) {
        int bright = argb(255, 240, 250, 255);
        int mid = argb(255, 220, 240, 252);
        int dim = argb(255, 200, 230, 248);
        // Center crystal
        for (int dy = -2; dy <= 2; dy++)
            for (int dx = -2; dx <= 2; dx++)
                if (dx * dx + dy * dy <= 5)
                    fillFace(img, cx + dx, cy + dy, bright);

        // 6 arms with detailed branches
        for (int arm = 0; arm < 6; arm++) {
            double angle = arm * Math.PI / 3.0;
            for (int i = 1; i <= size; i++) {
                float t = (float) i / size;
                int px = cx + (int)(Math.cos(angle) * i);
                int py = cy + (int)(Math.sin(angle) * i);
                int color = t < 0.4f ? bright : (t < 0.7f ? mid : dim);
                fillFace(img, px, py, color);
                // Width of arm (thicker near center)
                float width = 1.5f * (1 - t * 0.6f);
                double perpAngle = angle + Math.PI / 2;
                for (int w = 1; w <= (int) width; w++) {
                    int wx = px + (int)(Math.cos(perpAngle) * w);
                    int wy = py + (int)(Math.sin(perpAngle) * w);
                    blendFace(img, wx, wy, mid, 0.6f);
                    wx = px - (int)(Math.cos(perpAngle) * w);
                    wy = py - (int)(Math.sin(perpAngle) * w);
                    blendFace(img, wx, wy, mid, 0.6f);
                }

                // Branches at 1/4, 1/2, 3/4
                if (i == size / 4 || i == size / 2 || i == size * 3 / 4) {
                    int branchLen = (int)(size * 0.35f * (1 - t));
                    for (int side = -1; side <= 1; side += 2) {
                        double ba = angle + side * Math.PI / 4;
                        for (int bLen = 1; bLen <= branchLen; bLen++) {
                            float bt = (float) bLen / branchLen;
                            int bx = px + (int)(Math.cos(ba) * bLen);
                            int by = py + (int)(Math.sin(ba) * bLen);
                            blendFace(img, bx, by, bt < 0.5f ? mid : dim, 0.8f * (1 - bt * 0.4f));
                            // Sub-branches
                            if (bLen == branchLen / 2 && branchLen > 3) {
                                double sba = ba + side * Math.PI / 5;
                                for (int sb = 1; sb <= branchLen / 3; sb++) {
                                    blendFace(img, bx + (int)(Math.cos(sba) * sb),
                                        by + (int)(Math.sin(sba) * sb), dim, 0.5f);
                                }
                            }
                        }
                    }
                }
            }
            // Arm tip crystal
            int tipX = cx + (int)(Math.cos(angle) * size);
            int tipY = cy + (int)(Math.sin(angle) * size);
            fillFace(img, tipX, tipY, bright);
        }
    }

    private static void drawIceCrystal(NativeImage img, int cx, int cy, int size) {
        int color = argb(255, 210, 240, 255);
        // Hexagonal outline
        for (int i = 0; i < 6; i++) {
            double a1 = i * Math.PI / 3.0;
            double a2 = (i + 1) * Math.PI / 3.0;
            int x0 = cx + (int)(Math.cos(a1) * size);
            int y0 = cy + (int)(Math.sin(a1) * size);
            int x1 = cx + (int)(Math.cos(a2) * size);
            int y1 = cy + (int)(Math.sin(a2) * size);
            drawLine(img, x0, y0, x1, y1, color, 0.7f);
        }
        fillFace(img, cx, cy, argb(255, 235, 248, 255));
    }

    private static void drawLine(NativeImage img, int x0, int y0, int x1, int y1, int color, float alpha) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int steps = Math.max(dx, dy);
        if (steps == 0) { blendFace(img, x0, y0, color, alpha); return; }
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            int px = (int)(x0 + (x1 - x0) * t);
            int py = (int)(y0 + (y1 - y0) * t);
            blendFace(img, px, py, color, alpha);
        }
    }

    // ========== FLAME ==========
    private static void generateFlame(NativeImage img) {
        // Multi-layered fire gradient with noise turbulence
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            for (int x = 0; x < FW; x++) {
                // Turbulent noise for fire look
                float turb = fbm(x * 0.08f, y * 0.06f, 4) * 0.18f;
                float turb2 = fbm(x * 0.12f + 50, y * 0.1f + 50, 3) * 0.1f;
                float f = Math.max(0, Math.min(1, t + turb + turb2));

                int r, g, b;
                if (f < 0.15f) {
                    // White-yellow core (top)
                    float ff = f / 0.15f;
                    r = 255; g = lerp(255, 245, ff); b = lerp(200, 100, ff);
                } else if (f < 0.3f) {
                    // Bright yellow-orange
                    float ff = (f - 0.15f) / 0.15f;
                    r = 255; g = lerp(245, 185, ff); b = lerp(100, 30, ff);
                } else if (f < 0.55f) {
                    // Orange
                    float ff = (f - 0.3f) / 0.25f;
                    r = lerp(255, 220, ff); g = lerp(185, 80, ff); b = lerp(30, 10, ff);
                } else if (f < 0.75f) {
                    // Deep orange-red
                    float ff = (f - 0.55f) / 0.2f;
                    r = lerp(220, 170, ff); g = lerp(80, 30, ff); b = lerp(10, 5, ff);
                } else {
                    // Dark red-black embers
                    float ff = (f - 0.75f) / 0.25f;
                    r = lerp(170, 60, ff); g = lerp(30, 8, ff); b = lerp(5, 0, ff);
                }
                fillFace(img, x, y, argb(255, r, g, b));
            }
            float t2 = (float) y / (FH - 1);
            fillEdges(img, y, argb(255, lerp(255, 60, t2), lerp(220, 8, t2), lerp(120, 0, t2)));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 255, 248, 180));

        // Flame tongues rising from bottom
        int[][] tongues = {{10, 30}, {25, 25}, {40, 35}, {55, 28}, {70, 22}, {18, 20}, {48, 32}, {65, 26}};
        for (int[] t : tongues) {
            int tx = t[0], h = t[1];
            for (int dy = 0; dy < h; dy++) {
                int y = FH - 1 - dy;
                float f = (float) dy / h;
                // Parabolic width falloff
                int w = Math.max(1, (int)(7 * (1 - f * f)));
                float sway = (float)(Math.sin(dy * 0.18 + tx * 0.3) * 2.5);
                for (int dx = -w; dx <= w; dx++) {
                    int x = tx + dx + (int) sway;
                    float edgeFade = 1.0f - (float) Math.abs(dx) / w;
                    int cr = 255, cg = (int)(220 * (1 - f * 0.7f)), cb = (int)(60 * (1 - f));
                    blendFace(img, x, y, argb(255, cr, cg, cb), 0.35f * (1 - f * 0.5f) * edgeFade);
                }
            }
        }

        // Hot white spots (embers in the core)
        int[][] hotSpots = {{30, 15}, {50, 20}, {15, 25}, {60, 10}, {40, 8}, {20, 12}};
        for (int[] hs : hotSpots) {
            int hx = hs[0], hy = hs[1];
            for (int dy = -3; dy <= 3; dy++) {
                for (int dx = -3; dx <= 3; dx++) {
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d <= 3) {
                        float glow = (1 - d / 3.0f);
                        blendFace(img, hx + dx, hy + dy, argb(255, 255, 255, 200), glow * 0.5f);
                    }
                }
            }
        }

        // Ember sparkle dots
        for (int i = 0; i < 20; i++) {
            int ex = (int)(pseudoRand(i * 11, 7) * FW);
            int ey = (int)(pseudoRand(i * 3, 23) * FH);
            float brightness = pseudoRand(i, 99);
            if (brightness > 0.4f) {
                fillFace(img, ex, ey, argb(255, 255, (int)(200 * brightness), (int)(50 * brightness)));
            }
        }
    }

    // ========== OCEAN ==========
    private static void generateOcean(NativeImage img) {
        // Deep ocean gradient with depth layers
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            for (int x = 0; x < FW; x++) {
                // Multi-frequency wave noise
                float wave1 = (float)(Math.sin(x * 0.06 + y * 0.03) * 0.04);
                float wave2 = (float)(Math.sin(x * 0.12 - y * 0.08 + 1.5) * 0.02);
                float tt = Math.max(0, Math.min(1, t + wave1 + wave2));
                int r = lerp(15, 3, tt), g = lerp(95, 22, tt), b = lerp(185, 65, tt);
                fillFace(img, x, y, argb(255, r, g, b));
            }
            fillEdges(img, y, argb(255, lerp(15, 3, t), lerp(95, 22, t), lerp(185, 65, t)));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 15, 95, 185));

        // Multiple wave crest layers with foam
        for (int layer = 0; layer < 7; layer++) {
            int baseY = 8 + layer * 17;
            float freq = 0.12f + layer * 0.02f;
            float amp = 3.5f - layer * 0.3f;
            int bright = 210 - layer * 18;

            for (int x = 0; x < FW; x++) {
                int wy = baseY + (int)(Math.sin(x * freq + layer * 2.1) * amp);
                int wy2 = baseY + (int)(Math.sin(x * freq * 0.7 + layer * 1.3 + 1) * amp * 0.6);
                int finalWy = (wy + wy2) / 2;

                // Wave crest line
                blendFace(img, x, finalWy, argb(255, bright, bright + 10, 255), 0.35f);
                blendFace(img, x, finalWy + 1, argb(255, bright - 20, bright - 10, 245), 0.2f);
                blendFace(img, x, finalWy + 2, argb(255, bright - 40, bright - 30, 235), 0.1f);

                // Foam spots
                if ((x + layer * 7) % 5 == 0) {
                    blendFace(img, x, finalWy - 1, argb(255, 230, 245, 255), 0.5f);
                    blendFace(img, x + 1, finalWy - 1, argb(255, 220, 240, 252), 0.35f);
                    blendFace(img, x - 1, finalWy - 1, argb(255, 220, 240, 252), 0.3f);
                }
            }
        }

        // Caustic light patterns (underwater light ripples)
        for (int y = FH / 2; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                float caustic = (float)(Math.sin(x * 0.25 + y * 0.15) * Math.sin(x * 0.1 - y * 0.2 + 1));
                if (caustic > 0.5f) {
                    blendFace(img, x, y, argb(255, 60, 140, 220), (caustic - 0.5f) * 0.2f);
                }
            }
        }

        // Small fish silhouettes
        drawFish(img, 55, 90, 6, true);
        drawFish(img, 20, 110, 5, false);
        drawFish(img, 65, 120, 4, true);
    }

    private static void drawFish(NativeImage img, int cx, int cy, int size, boolean facingRight) {
        int dir = facingRight ? 1 : -1;
        // Body (oval)
        for (int dy = -size / 2; dy <= size / 2; dy++) {
            int w = (int)(size * Math.sqrt(1 - (float)(dy * dy) / (size * size / 4.0f)));
            for (int dx = -w; dx <= w; dx++) {
                blendFace(img, cx + dx * dir, cy + dy, argb(255, 20, 60, 120), 0.25f);
            }
        }
        // Tail
        for (int dy = -size / 2; dy <= size / 2; dy++) {
            int tw = Math.abs(dy);
            for (int dx = 0; dx <= tw; dx++) {
                blendFace(img, cx - (size + dx) * dir, cy + dy, argb(255, 15, 50, 110), 0.2f);
            }
        }
    }

    // ========== EMERALD ==========
    private static void generateEmerald(NativeImage img) {
        // Rich green gradient with crystalline depth
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            for (int x = 0; x < FW; x++) {
                float n = fbm(x * 0.05f, y * 0.05f, 3) * 0.06f;
                float tt = Math.max(0, Math.min(1, t + n));
                int r = lerp(25, 6, tt), g = lerp(165, 75, tt), b = lerp(55, 20, tt);
                fillFace(img, x, y, argb(255, r, g, b));
            }
            fillEdges(img, y, argb(255, lerp(25, 6, t), lerp(165, 75, t), lerp(55, 20, t)));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 25, 165, 55));

        // Large faceted gems with light refraction
        drawGem(img, 20, 18, 12);
        drawGem(img, 55, 14, 14);
        drawGem(img, 35, 50, 11);
        drawGem(img, 15, 78, 12);
        drawGem(img, 62, 68, 10);
        drawGem(img, 40, 98, 14);
        drawGem(img, 70, 105, 8);

        // Small gem accents
        drawGem(img, 8, 40, 6);
        drawGem(img, 72, 35, 5);
        drawGem(img, 28, 115, 6);
        drawGem(img, 58, 118, 5);

        // Sparkle points with glow
        int[][] sparkles = {
            {10, 8}, {68, 28}, {30, 35}, {75, 55}, {5, 60}, {50, 75},
            {22, 90}, {65, 95}, {12, 108}, {45, 122}, {72, 118}
        };
        for (int[] s : sparkles) {
            fillFace(img, s[0], s[1], argb(255, 200, 255, 220));
            blendFace(img, s[0] + 1, s[1], argb(255, 160, 240, 190), 0.4f);
            blendFace(img, s[0] - 1, s[1], argb(255, 160, 240, 190), 0.4f);
            blendFace(img, s[0], s[1] + 1, argb(255, 160, 240, 190), 0.4f);
            blendFace(img, s[0], s[1] - 1, argb(255, 160, 240, 190), 0.4f);
        }

        // Neon green edge glow
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                float edgeDist = Math.min(Math.min(x, FW - 1 - x), Math.min(y, FH - 1 - y)) / 8.0f;
                if (edgeDist < 1.0f) {
                    blendFace(img, x, y, argb(255, 80, 255, 120), (1 - edgeDist) * 0.2f);
                }
            }
        }
    }

    private static void drawGem(NativeImage img, int cx, int cy, int size) {
        // Diamond rhombus with faceted lighting
        for (int dy = -size; dy <= size; dy++) {
            int hw = size - Math.abs(dy);
            for (int dx = -hw; dx <= hw; dx++) {
                float dist = (Math.abs(dx) + Math.abs(dy)) / (float) size;

                // 4-quadrant facet lighting
                int baseG;
                if (dy < 0 && dx <= 0) baseG = 230;       // Top-left: brightest
                else if (dy < 0) baseG = 205;              // Top-right: bright
                else if (dx <= 0) baseG = 175;             // Bottom-left: medium
                else baseG = 145;                           // Bottom-right: darkest

                int r = (int)(baseG * 0.20f), g = baseG, b = (int)(baseG * 0.45f);

                // Edge highlight (gem outline catch-light)
                if (dist > 0.85f) {
                    r += 35; g += 35; b += 35;
                }
                // Inner facet line
                if (Math.abs(dx) <= 1 && dy < 0 && dy > -size) {
                    r += 15; g += 20; b += 15;
                }
                if (Math.abs(dy) <= 1 && dx != 0) {
                    r += 10; g += 15; b += 10;
                }

                fillFace(img, cx + dx, cy + dy, argb(255, clamp(r), clamp(g), clamp(b)));
            }
        }

        // Center highlight (bright reflection point)
        for (int dy = -2; dy <= 1; dy++) {
            for (int dx = -2; dx <= 1; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d <= 2) {
                    float bright = 1 - d / 2.5f;
                    blendFace(img, cx + dx, cy + dy, argb(255, 210, 255, 230), bright * 0.7f);
                }
            }
        }

        // Diagonal highlight streak
        for (int i = -size / 2; i <= 0; i++) {
            int hx = cx + i, hy = cy + i;
            blendFace(img, hx, hy, argb(255, 190, 250, 215), 0.3f);
        }
    }

    // ========== SUNSET ==========
    private static void generateSunset(NativeImage img) {
        // Smooth golden hour palette with wide bands
        for (int y = 0; y < FH; y++) {
            float t = (float) y / (FH - 1);
            int r, g, b;
            if (t < 0.15f) {
                float f = t / 0.15f;
                r = 255; g = lerp(215, 180, f); b = lerp(95, 60, f);
            } else if (t < 0.3f) {
                float f = (t - 0.15f) / 0.15f;
                r = 255; g = lerp(180, 140, f); b = lerp(60, 40, f);
            } else if (t < 0.5f) {
                float f = (t - 0.3f) / 0.2f;
                r = lerp(255, 235, f); g = lerp(140, 80, f); b = lerp(40, 70, f);
            } else if (t < 0.7f) {
                float f = (t - 0.5f) / 0.2f;
                r = lerp(235, 180, f); g = lerp(80, 50, f); b = lerp(70, 115, f);
            } else if (t < 0.85f) {
                float f = (t - 0.7f) / 0.15f;
                r = lerp(180, 120, f); g = lerp(50, 35, f); b = lerp(115, 140, f);
            } else {
                float f = (t - 0.85f) / 0.15f;
                r = lerp(120, 70, f); g = lerp(35, 20, f); b = lerp(140, 115, f);
            }
            for (int x = 0; x < FW; x++) {
                float n = fbm(x * 0.03f, y * 0.03f, 2) * 0.03f;
                int rr = clamp(r + (int)(n * 30));
                int gg = clamp(g + (int)(n * 15));
                int bb = clamp(b + (int)(n * 15));
                fillFace(img, x, y, argb(255, rr, gg, bb));
            }
            fillEdges(img, y, argb(255, r, g, b));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 255, 215, 95));

        // Large sun with corona
        int sunCx = 40, sunCy = 25, sunR = 16;
        // Corona glow (outer rings)
        for (int dy = -sunR - 14; dy <= sunR + 14; dy++) {
            for (int dx = -sunR - 14; dx <= sunR + 14; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > sunR && d < sunR + 14) {
                    float glow = 1.0f - (d - sunR) / 14.0f;
                    glow = glow * glow; // Quadratic falloff
                    blendFace(img, sunCx + dx, sunCy + dy, argb(255, 255, 230, 160), glow * 0.35f);
                }
            }
        }
        // Sun disc
        for (int dy = -sunR; dy <= sunR; dy++) {
            for (int dx = -sunR; dx <= sunR; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d <= sunR) {
                    float bright = 1.0f - d / sunR * 0.25f;
                    fillFace(img, sunCx + dx, sunCy + dy,
                        argb(255, 255, (int)(248 * bright), (int)(195 * bright)));
                }
            }
        }

        // Sun rays (long radiating beams)
        for (int ray = 0; ray < 16; ray++) {
            double angle = ray * Math.PI / 8.0;
            float rayLen = 12 + pseudoRand(ray, 42) * 8;
            float rayWidth = 1.5f + pseudoRand(ray, 17) * 1.5f;
            for (int i = sunR + 1; i < sunR + (int) rayLen; i++) {
                float fade = 1.0f - (float)(i - sunR) / rayLen;
                fade = fade * fade;
                int px = sunCx + (int)(Math.cos(angle) * i);
                int py = sunCy + (int)(Math.sin(angle) * i);
                for (int w = (int)-rayWidth; w <= (int) rayWidth; w++) {
                    int wx = px + (int)(Math.cos(angle + Math.PI / 2) * w);
                    int wy = py + (int)(Math.sin(angle + Math.PI / 2) * w);
                    float wFade = 1.0f - (float) Math.abs(w) / rayWidth;
                    blendFace(img, wx, wy, argb(255, 255, 235, 165), fade * wFade * 0.3f);
                }
            }
        }

        // Cloud silhouettes with internal shading
        drawCloud(img, 14, 75, 18);
        drawCloud(img, 55, 88, 14);
        drawCloud(img, 35, 100, 10);

        // Horizon line glow
        for (int x = 0; x < FW; x++) {
            float glow = (float)(Math.sin(x * 0.1) * 0.5 + 0.5) * 0.15f;
            for (int dy = -3; dy <= 3; dy++) {
                float dFade = 1 - (float) Math.abs(dy) / 3.0f;
                blendFace(img, x, FH / 2 + dy, argb(255, 255, 200, 120), glow * dFade);
            }
        }
    }

    private static void drawCloud(NativeImage img, int cx, int cy, int size) {
        // Multi-circle cloud with shading
        int[][] circles = {
            {0, 0, size},
            {-size * 2 / 3, 2, size * 3 / 4},
            {size * 2 / 3, 2, size * 3 / 4},
            {-size / 3, -size / 4, size * 2 / 3},
            {size / 3, -size / 4, size * 2 / 3}
        };
        for (int[] c : circles) {
            int ccx = cx + c[0], ccy = cy + c[1], r = c[2];
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d <= r) {
                        float shade = d / r;
                        // Darker at bottom, lit at top
                        int dark = dy > 0 ? 75 : 55;
                        blendFace(img, ccx + dx, ccy + dy, argb(255, dark, dark / 2, dark - 10), 0.3f * (1 - shade * 0.3f));
                    }
                }
            }
        }
    }

    // ========== GALAXY ==========
    private static void generateGalaxy(NativeImage img) {
        int cx = FW / 2, cy = FH / 2;

        // Multi-arm spiral galaxy with nebula clouds
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                float dx = x - cx, dy = (y - cy) * 0.6f; // Stretch vertically to make it elliptical
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);

                // Two spiral arms
                float spiral1 = (float)(Math.sin(angle * 2 + dist * 0.06 - 0.5) * 0.5 + 0.5);
                float spiral2 = (float)(Math.sin(angle * 2 + dist * 0.06 + Math.PI - 0.5) * 0.5 + 0.5);
                float spiral = Math.max(spiral1, spiral2);
                float falloff = Math.max(0, 1.0f - dist / 45.0f);
                spiral *= falloff * falloff;

                // Nebula color (purple-blue-pink tones)
                float neb1 = fbm(x * 0.04f, y * 0.04f, 3);
                float neb2 = fbm(x * 0.06f + 100, y * 0.06f + 100, 2);

                int r = 6 + (int)(spiral * 40 + neb1 * 20);
                int g = 2 + (int)(spiral * 12 + neb2 * 8);
                int b = 14 + (int)(spiral * 65 + neb1 * 30);

                // Core brightness
                float core = Math.max(0, 1.0f - dist / 12.0f);
                core = core * core * core;
                r += (int)(core * 80);
                g += (int)(core * 60);
                b += (int)(core * 50);

                fillFace(img, x, y, argb(255, clamp(r), clamp(g), clamp(b)));
            }
            fillEdges(img, y, argb(255, 6, 2, 14));
        }
        for (int x = 0; x < FW; x++) fillTop(img, x, argb(255, 6, 2, 14));

        // Nebula cloud patches (colorful regions)
        drawNebulaPatch(img, 15, 30, 12, argb(255, 80, 20, 120)); // Purple
        drawNebulaPatch(img, 60, 25, 10, argb(255, 30, 50, 130)); // Blue
        drawNebulaPatch(img, 25, 90, 14, argb(255, 100, 25, 80)); // Magenta
        drawNebulaPatch(img, 55, 100, 11, argb(255, 40, 60, 120)); // Teal-blue

        // Stars with cross-flare effect
        drawCrossFlare(img, 12, 14, 5, argb(255, 255, 225, 255));
        drawCrossFlare(img, 55, 35, 4, argb(255, 210, 210, 255));
        drawCrossFlare(img, 24, 60, 5, argb(255, 255, 205, 230));
        drawCrossFlare(img, 68, 75, 4, argb(255, 225, 245, 255));
        drawCrossFlare(img, 40, 108, 5, argb(255, 235, 215, 255));

        // Medium stars
        drawStar(img, 30, 20, 3, argb(255, 220, 220, 255));
        drawStar(img, 65, 50, 3, argb(255, 200, 200, 245));
        drawStar(img, 10, 80, 3, argb(255, 240, 230, 255));
        drawStar(img, 50, 70, 2, argb(255, 210, 210, 250));

        // Dense star field
        int[][] stars = {
            {5, 6}, {18, 8}, {35, 5}, {50, 10}, {72, 12}, {8, 28}, {42, 22},
            {62, 30}, {75, 38}, {3, 45}, {28, 42}, {55, 48}, {70, 55},
            {12, 55}, {38, 58}, {65, 62}, {8, 72}, {25, 75}, {50, 78},
            {74, 80}, {15, 88}, {42, 85}, {60, 90}, {5, 100}, {30, 102},
            {55, 98}, {72, 105}, {18, 112}, {45, 115}, {65, 118},
            {10, 120}, {35, 125}, {58, 122}, {75, 125}
        };
        for (int[] s : stars) {
            float bright = pseudoRand(s[0] * 3, s[1] * 7);
            int starBright = 170 + (int)(bright * 85);
            int blueShift = (int)(bright * 40);
            fillFace(img, s[0], s[1], argb(255, starBright - blueShift / 2, starBright - blueShift / 3, starBright + blueShift));
        }

        // Bright galaxy core glow
        for (int dy = -8; dy <= 8; dy++) {
            for (int dx = -10; dx <= 10; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy * 1.5f);
                if (d < 8) {
                    float glow = (1 - d / 8.0f);
                    glow = glow * glow;
                    blendFace(img, cx + dx, cy + dy, argb(255, 200, 180, 220), glow * 0.4f);
                }
            }
        }
    }

    private static void drawNebulaPatch(NativeImage img, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < radius) {
                    float n = fbm((cx + dx) * 0.1f, (cy + dy) * 0.1f, 2);
                    float alpha = (1 - d / radius) * n * 0.3f;
                    if (alpha > 0.01f) {
                        blendFace(img, cx + dx, cy + dy, color, alpha);
                    }
                }
            }
        }
    }

    // ========== VIGNETTE HELPER ==========
    private static void applyVignette(NativeImage img, int color, float maxAlpha) {
        for (int y = 0; y < FH; y++) {
            for (int x = 0; x < FW; x++) {
                float dx = (x - FW / 2.0f) / (FW / 2.0f);
                float dy = (y - FH / 2.0f) / (FH / 2.0f);
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float vignette = Math.max(0, dist - 0.6f) / 0.8f;
                if (vignette > 0) {
                    blendFace(img, x, y, color, Math.min(maxAlpha, vignette * maxAlpha));
                }
            }
        }
    }

    // ========== HELPERS ==========
    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    private static int lerp(int a, int b, float t) { t = Math.max(0, Math.min(1, t)); return (int)(a + (b - a) * t); }
    private static float lerpF(float a, float b, float t) { return a + (b - a) * t; }

    static void blend(NativeImage img, int x, int y, int color, float alpha) {
        int w = img.getWidth(), h = img.getHeight();
        if (x < 0 || x >= w || y < 0 || y >= h) return;
        int e = img.getColorArgb(x, y);
        if (((e >> 24) & 0xFF) == 0) return;
        int er = (e >> 16) & 0xFF, eg = (e >> 8) & 0xFF, eb = e & 0xFF;
        int nr = (color >> 16) & 0xFF, ng = (color >> 8) & 0xFF, nb = color & 0xFF;
        img.setColorArgb(x, y, argb(255, (int)(er + (nr - er) * alpha), (int)(eg + (ng - eg) * alpha), (int)(eb + (nb - eb) * alpha)));
    }
}
