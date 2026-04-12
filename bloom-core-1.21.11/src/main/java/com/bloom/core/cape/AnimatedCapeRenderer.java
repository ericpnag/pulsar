package com.bloom.core.cape;

import com.bloom.core.module.modules.CosmeticsCape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class AnimatedCapeRenderer {
    private static final Identifier ANIMATED_CAPE_ID = Identifier.of("bloom-core", "animated_cape_dynamic");
    private static NativeImageBackedTexture dynamicTexture;
    private static NativeImage baseImage;
    private static String loadedCapeFile = null;
    private static int tickCounter = 0;

    // Cape face dimensions at 8x scale (local coords 0-79, 0-127)
    private static final int SCALE = 8;
    private static final int FW = 10 * SCALE, FH = 16 * SCALE;  // 80x128
    private static final int FX = 1 * SCALE, FY = 1 * SCALE;    // 8, 8
    private static final int BX = 12 * SCALE, BY = 1 * SCALE;   // 96, 8

    // Particle system - generous count for premium effects
    private static final int P_COUNT = 24;
    private static final float[] pX = new float[P_COUNT];
    private static final float[] pY = new float[P_COUNT];
    private static final float[] pDX = new float[P_COUNT];
    private static final float[] pDY = new float[P_COUNT];
    private static final float[] pPhase = new float[P_COUNT];
    private static final float[] pSize = new float[P_COUNT];
    private static final float[] pRot = new float[P_COUNT];
    private static final float[] pLife = new float[P_COUNT];
    private static boolean particlesInited = false;

    // Secondary particle array for dual-layer effects
    private static final int S_COUNT = 12;
    private static final float[] sX = new float[S_COUNT];
    private static final float[] sY = new float[S_COUNT];
    private static final float[] sDX = new float[S_COUNT];
    private static final float[] sDY = new float[S_COUNT];
    private static final float[] sPhase = new float[S_COUNT];
    private static final float[] sLife = new float[S_COUNT];

    public static Identifier getAnimatedCapeId() {
        return ANIMATED_CAPE_ID;
    }

    public static void tick() {
        if (!CosmeticsCape.showCape || CosmeticsCape.capeFile == null) return;

        String capeFile = CosmeticsCape.capeFile;

        if (!capeFile.equals(loadedCapeFile)) {
            loadBaseImage(capeFile);
            particlesInited = false;
        }

        if (baseImage == null) return;

        tickCounter++;
        updateAnimatedFrame(capeFile);
    }

    private static void loadBaseImage(String capeFile) {
        try {
            String path = "/assets/bloom-core/textures/cape/" + capeFile;
            InputStream stream = AnimatedCapeRenderer.class.getResourceAsStream(path);
            if (stream == null) return;

            if (baseImage != null) baseImage.close();
            baseImage = NativeImage.read(stream);
            stream.close();

            // Generate detailed base texture
            CapeTextureGenerator.generateBase(baseImage, capeFile);

            if (dynamicTexture != null) dynamicTexture.close();
            int w = baseImage.getWidth(), h = baseImage.getHeight();
            NativeImage target = new NativeImage(w, h, false);
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    target.setColorArgb(x, y, baseImage.getColorArgb(x, y));
            dynamicTexture = new NativeImageBackedTexture(() -> "bloom-core:animated_cape_dynamic", target);
            MinecraftClient.getInstance().getTextureManager().registerTexture(ANIMATED_CAPE_ID, dynamicTexture);

            loadedCapeFile = capeFile;
            tickCounter = 0;
        } catch (IOException e) {
            baseImage = null;
        }
    }

    private static void updateAnimatedFrame(String capeFile) {
        if (dynamicTexture == null || baseImage == null) return;
        NativeImage image = dynamicTexture.getImage();
        if (image == null) return;

        // Copy base
        int w = baseImage.getWidth(), h = baseImage.getHeight();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                image.setColorArgb(x, y, baseImage.getColorArgb(x, y));

        // Per-cape animation
        switch (capeFile) {
            case "bloom_cape.png" -> animateCherryBlossom(image);
            case "midnight_cape.png" -> animateMidnight(image);
            case "frost_cape.png" -> animateFrost(image);
            case "flame_cape.png" -> animateFlame(image);
            case "ocean_cape.png" -> animateOcean(image);
            case "emerald_cape.png" -> animateEmerald(image);
            case "sunset_cape.png" -> animateSunset(image);
            case "galaxy_cape.png" -> animateGalaxy(image);
        }

        dynamicTexture.upload();
    }

    // --- Helpers ---
    private static void faceBlend(NativeImage img, int lx, int ly, int color, float alpha) {
        if (lx < 0 || lx >= FW || ly < 0 || ly >= FH) return;
        blend(img, FX + lx, FY + ly, color, alpha);
        blend(img, BX + lx, BY + ly, color, alpha);
    }

    private static void faceSet(NativeImage img, int lx, int ly, int argb) {
        if (lx < 0 || lx >= FW || ly < 0 || ly >= FH) return;
        img.setColorArgb(FX + lx, FY + ly, argb);
        img.setColorArgb(BX + lx, BY + ly, argb);
    }

    private static void blend(NativeImage img, int x, int y, int color, float alpha) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) return;
        int e = img.getColorArgb(x, y);
        if (((e >> 24) & 0xFF) == 0) return;
        int er = (e >> 16) & 0xFF, eg = (e >> 8) & 0xFF, eb = e & 0xFF;
        int nr = (color >> 16) & 0xFF, ng = (color >> 8) & 0xFF, nb = color & 0xFF;
        int r = Math.min(255, (int)(er + (nr - er) * alpha));
        int g = Math.min(255, (int)(eg + (ng - eg) * alpha));
        int b = Math.min(255, (int)(eb + (nb - eb) * alpha));
        img.setColorArgb(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
    }

    private static int argb(int a, int r, int g, int b) {
        return (Math.max(0, Math.min(255, a)) << 24) | (Math.max(0, Math.min(255, r)) << 16) |
            (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));
    }

    private static float noise2D(float x, float y) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        float fx = x - ix, fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        float a = pseudoRand(ix, iy), b = pseudoRand(ix + 1, iy);
        float c = pseudoRand(ix, iy + 1), d = pseudoRand(ix + 1, iy + 1);
        return lerp(lerp(a, b, fx), lerp(c, d, fx), fy);
    }

    private static float pseudoRand(int x, int y) {
        int n = x + y * 57;
        n = (n << 13) ^ n;
        return (1.0f - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f) * 0.5f + 0.5f;
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    // ========== CHERRY BLOSSOM - drifting rotating petals ==========
    private static void animateCherryBlossom(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(123);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = -r.nextFloat() * FH; // Start above, stagger entry
                pDX[i] = (r.nextFloat() - 0.5f) * 0.15f;
                pDY[i] = 0.12f + r.nextFloat() * 0.18f;
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 3 + r.nextFloat() * 4; // Petal size 3-7px
                pRot[i] = r.nextFloat() * 6.28f;
                pLife[i] = 0.5f + r.nextFloat() * 0.5f; // Transparency
            }
            particlesInited = true;
        }

        // Update and draw each petal
        for (int i = 0; i < P_COUNT; i++) {
            // Gentle wind sway (sinusoidal drift)
            float windX = (float) Math.sin(tickCounter * 0.04 + pPhase[i]) * 0.25f;
            float windY = (float) Math.cos(tickCounter * 0.02 + pPhase[i] * 0.7) * 0.05f;
            pX[i] += pDX[i] + windX;
            pY[i] += pDY[i] + windY;
            pRot[i] += 0.03f + (float) Math.sin(tickCounter * 0.05 + pPhase[i]) * 0.02f;

            // Wrap around
            if (pY[i] >= FH + 5) {
                pY[i] = -pSize[i] * 2;
                pX[i] = new Random().nextFloat() * FW;
            }
            if (pX[i] < -5) pX[i] += FW + 10;
            if (pX[i] >= FW + 5) pX[i] -= FW + 10;

            // Draw petal shape (rotated ellipse)
            int px = (int) pX[i], py = (int) pY[i];
            float sz = pSize[i];
            float cos = (float) Math.cos(pRot[i]), sin = (float) Math.sin(pRot[i]);
            float alpha = pLife[i] * (0.45f + (float) Math.sin(tickCounter * 0.06 + pPhase[i]) * 0.15f);

            for (int dy = (int)(-sz - 1); dy <= (int)(sz + 1); dy++) {
                for (int dx = (int)(-sz - 1); dx <= (int)(sz + 1); dx++) {
                    // Rotate into petal space
                    float lx = dx * cos - dy * sin;
                    float ly = dx * sin + dy * cos;
                    float ex = lx / sz, ey = ly / (sz * 0.45f);
                    float dist = ex * ex + ey * ey;
                    if (dist <= 1.0f) {
                        // Color gradient within petal
                        float inner = 1 - dist;
                        int r = 255, g = 205 + (int)(inner * 30), b = 220 + (int)(inner * 20);
                        float pa = alpha * (1 - dist * 0.4f);
                        faceBlend(img, px + dx, py + dy, argb(255, r, g, b), pa);
                    }
                }
            }
        }

        // Soft shimmer sweep (diagonal light band)
        float shimmer = (tickCounter * 0.6f) % (FW + FH + 20) - 10;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - shimmer);
                if (dist < 5) {
                    float intensity = (float)(Math.cos(dist / 5 * Math.PI) + 1) * 0.08f;
                    faceBlend(img, lx, ly, 0xFFFFFFFF, intensity);
                }
            }
        }

        // Gentle color breathing (warm/cool shift)
        float breath = (float)(Math.sin(tickCounter * 0.015) * 0.03);
        if (Math.abs(breath) > 0.005f) {
            int overlay = breath > 0 ? 0xFFFFDDEE : 0xFFEEDDFF;
            for (int ly = 0; ly < FH; ly += 2) {
                for (int lx = 0; lx < FW; lx += 2) {
                    faceBlend(img, lx, ly, overlay, Math.abs(breath));
                }
            }
        }
    }

    // ========== MIDNIGHT - twinkling stars + shooting star + aurora ==========
    private static void animateMidnight(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(456);
            // Stars that twinkle
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
                pDY[i] = 0.03f + r.nextFloat() * 0.07f; // twinkle speed
                pSize[i] = 1 + r.nextFloat() * 3; // Star flare size
            }
            // Shooting star state
            sX[0] = -10; sY[0] = -10; sLife[0] = 0; // inactive
            particlesInited = true;
        }

        // Twinkling stars with cross-flare pulsation
        for (int i = 0; i < P_COUNT; i++) {
            float brightness = (float)(Math.sin(tickCounter * pDY[i] * 2.5 + pPhase[i]) * 0.5 + 0.5);
            brightness = brightness * brightness; // Sharper pulse
            int lx = (int) pX[i], ly = (int) pY[i];
            int sr = 190 + (int)(brightness * 65);
            int sb = 210 + (int)(brightness * 45);

            // Center bright point
            faceBlend(img, lx, ly, argb(255, sr, sr, sb), brightness * 0.9f);

            // Cross flare when bright
            if (brightness > 0.5f) {
                float flareLen = pSize[i] * brightness;
                for (int f = 1; f <= (int) flareLen; f++) {
                    float fade = 1.0f - f / (flareLen + 1);
                    int fc = argb(255, sr - 20, sr - 20, sb);
                    faceBlend(img, lx + f, ly, fc, fade * brightness * 0.5f);
                    faceBlend(img, lx - f, ly, fc, fade * brightness * 0.5f);
                    faceBlend(img, lx, ly + f, fc, fade * brightness * 0.5f);
                    faceBlend(img, lx, ly - f, fc, fade * brightness * 0.5f);
                }
                // Diagonal flare (shorter)
                for (int f = 1; f <= (int)(flareLen * 0.5f); f++) {
                    float fade = 1.0f - f / (flareLen * 0.5f + 1);
                    int fc = argb(255, sr - 40, sr - 40, sb - 20);
                    faceBlend(img, lx + f, ly + f, fc, fade * brightness * 0.25f);
                    faceBlend(img, lx - f, ly - f, fc, fade * brightness * 0.25f);
                    faceBlend(img, lx + f, ly - f, fc, fade * brightness * 0.25f);
                    faceBlend(img, lx - f, ly + f, fc, fade * brightness * 0.25f);
                }
            }
        }

        // Shooting star (occasional, streaks across)
        sLife[0] -= 0.02f;
        if (sLife[0] <= 0 && tickCounter % 120 < 2) {
            // Launch new shooting star
            sX[0] = FW + 5;
            sY[0] = new Random().nextFloat() * FH * 0.4f;
            sDX[0] = -(1.5f + new Random().nextFloat() * 1.0f);
            sDY[0] = 0.4f + new Random().nextFloat() * 0.3f;
            sLife[0] = 1.0f;
        }
        if (sLife[0] > 0) {
            sX[0] += sDX[0];
            sY[0] += sDY[0];
            int sx = (int) sX[0], sy = (int) sY[0];

            // Bright head
            faceBlend(img, sx, sy, 0xFFFFFFFF, sLife[0] * 0.95f);
            faceBlend(img, sx + 1, sy, 0xFFEEEEFF, sLife[0] * 0.7f);
            faceBlend(img, sx, sy + 1, 0xFFEEEEFF, sLife[0] * 0.5f);

            // Trail (fading behind)
            for (int t = 1; t <= 15; t++) {
                float trailFade = sLife[0] * (1.0f - t / 16.0f);
                int tx = sx - (int)(sDX[0] * -t * 0.6f);
                int ty = sy - (int)(sDY[0] * -t * 0.6f);
                int bright = 200 - t * 12;
                faceBlend(img, tx, ty, argb(255, bright, bright, bright + 30), trailFade * 0.6f);
            }
            sLife[0] -= 0.008f;
        }

        // Aurora band at bottom - animated waves
        float auroraPhase = tickCounter * 0.035f;
        for (int ly = FH - 24; ly < FH; ly++) {
            float s = (float)(ly - (FH - 24)) / 24.0f;
            s = s * s; // Ease in
            for (int lx = 0; lx < FW; lx++) {
                float wave1 = (float)(Math.sin(lx * 0.08 + auroraPhase) * 0.5 + 0.5);
                float wave2 = (float)(Math.sin(lx * 0.05 - auroraPhase * 0.7 + 1.5) * 0.5 + 0.5);
                float wave3 = (float)(Math.sin(lx * 0.12 + auroraPhase * 1.3 + 3) * 0.5 + 0.5);
                // Purple layer
                faceBlend(img, lx, ly, argb(255, 80, 25, 130), s * wave1 * 0.25f);
                // Green layer
                faceBlend(img, lx, ly, argb(255, 30, 120, 80), s * wave2 * 0.15f);
                // Blue shimmer
                faceBlend(img, lx, ly, argb(255, 40, 60, 180), s * wave3 * 0.1f);
            }
        }
    }

    // ========== FROST - ice crystal growth + sparkle flashes ==========
    private static void animateFrost(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(789);
            // Sparkle flash positions
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 2 + r.nextFloat() * 3;
            }
            // Crystal growth seeds
            for (int i = 0; i < S_COUNT; i++) {
                sX[i] = r.nextInt(FW);
                sY[i] = r.nextInt(FH);
                sPhase[i] = r.nextFloat() * 6.28f;
                sLife[i] = r.nextFloat(); // Growth phase offset
            }
            particlesInited = true;
        }

        // Ice crystal growth animation (growing outward from seed points then fading)
        for (int i = 0; i < S_COUNT; i++) {
            float growCycle = (tickCounter * 0.012f + sLife[i]) % 2.0f; // 0-2 cycle
            float growRadius;
            float alpha;
            if (growCycle < 1.0f) {
                // Growing phase
                growRadius = growCycle * 12;
                alpha = 0.3f;
            } else {
                // Fading phase
                growRadius = 12;
                alpha = 0.3f * (2.0f - growCycle);
            }

            if (alpha > 0.01f) {
                int cx = (int) sX[i], cy = (int) sY[i];
                // 6-arm mini crystal
                for (int arm = 0; arm < 6; arm++) {
                    double angle = arm * Math.PI / 3.0 + sPhase[i];
                    for (int r = 0; r <= (int) growRadius; r++) {
                        float t = r / Math.max(1, growRadius);
                        int px = cx + (int)(Math.cos(angle) * r);
                        int py = cy + (int)(Math.sin(angle) * r);
                        faceBlend(img, px, py, 0xFFDDF0FF, alpha * (1 - t * 0.5f));
                        // Short branches
                        if (r == (int)(growRadius * 0.5f) && growRadius > 5) {
                            for (int side = -1; side <= 1; side += 2) {
                                double ba = angle + side * Math.PI / 4;
                                for (int bl = 1; bl <= (int)(growRadius * 0.25f); bl++) {
                                    faceBlend(img, px + (int)(Math.cos(ba) * bl),
                                        py + (int)(Math.sin(ba) * bl),
                                        0xFFCCE8FF, alpha * 0.6f);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sparkle flashes (sharp bright points that flash in and out)
        for (int i = 0; i < P_COUNT; i++) {
            float flash = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.055 + pPhase[i])), 6);
            int lx = (int) pX[i], ly = (int) pY[i];
            if (flash > 0.08f) {
                float sz = pSize[i] * flash;
                // Bright center
                faceBlend(img, lx, ly, 0xFFF0FAFF, flash * 0.95f);
                // Cross flare
                for (int f = 1; f <= (int) sz; f++) {
                    float fade = 1.0f - f / (sz + 1);
                    faceBlend(img, lx + f, ly, 0xFFDDEEFF, flash * fade * 0.5f);
                    faceBlend(img, lx - f, ly, 0xFFDDEEFF, flash * fade * 0.5f);
                    faceBlend(img, lx, ly + f, 0xFFDDEEFF, flash * fade * 0.5f);
                    faceBlend(img, lx, ly - f, 0xFFDDEEFF, flash * fade * 0.5f);
                }
            }
        }

        // Frost creep at edges (animated border frost growing inward)
        float frostPhase = (tickCounter * 0.02f) % 2.0f;
        float frostDepth = 6 + (float) Math.sin(tickCounter * 0.03) * 3;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float edgeDist = Math.min(Math.min(lx, FW - 1 - lx), Math.min(ly, FH - 1 - ly));
                if (edgeDist < frostDepth) {
                    float n = noise2D(lx * 0.15f + tickCounter * 0.01f, ly * 0.15f);
                    float frost = (1 - edgeDist / frostDepth) * n;
                    if (frost > 0.2f) {
                        faceBlend(img, lx, ly, 0xFFE8F4FF, (frost - 0.2f) * 0.4f);
                    }
                }
            }
        }

        // Diagonal shimmer sweep
        float shimmer = (tickCounter * 0.5f) % (FW + FH + 16) - 8;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - shimmer);
                if (dist < 6) {
                    float intensity = (float)(Math.cos(dist / 6 * Math.PI) + 1) * 0.1f;
                    faceBlend(img, lx, ly, 0xFFE0F0FF, intensity);
                }
            }
        }
    }

    // ========== FLAME - realistic upward fire with heat distortion ==========
    private static void animateFlame(NativeImage img) {
        // Per-pixel fire animation using scrolling noise
        float timeOffset = tickCounter * 0.08f;
        for (int ly = 0; ly < FH; ly++) {
            float yFactor = (float) ly / FH; // 0 at top, 1 at bottom
            for (int lx = 0; lx < FW; lx++) {
                // Scrolling fire noise (moves upward)
                float n1 = noise2D(lx * 0.06f, ly * 0.04f - timeOffset);
                float n2 = noise2D(lx * 0.12f + 50, ly * 0.08f - timeOffset * 1.3f);
                float fire = (n1 * 0.6f + n2 * 0.4f);

                // Stronger at bottom
                fire *= yFactor * yFactor;

                if (fire > 0.15f) {
                    float intensity = (fire - 0.15f) * 0.5f;
                    // Color by intensity (yellow-orange-red gradient)
                    int fr, fg, fb;
                    if (intensity > 0.25f) {
                        fr = 255; fg = 255; fb = 180; // White-yellow core
                    } else if (intensity > 0.15f) {
                        fr = 255; fg = 210; fb = 40; // Yellow-orange
                    } else {
                        fr = 255; fg = 140; fb = 10; // Orange
                    }
                    faceBlend(img, lx, ly, argb(255, fr, fg, fb), Math.min(0.35f, intensity));
                }
            }
        }

        // Heat distortion shimmer (subtle wavering)
        float heatTime = tickCounter * 0.12f;
        for (int ly = 0; ly < FH / 2; ly++) {
            float distortion = (float)(Math.sin(ly * 0.3 + heatTime) * Math.cos(ly * 0.15 - heatTime * 0.7));
            if (Math.abs(distortion) > 0.4f) {
                for (int lx = 0; lx < FW; lx++) {
                    faceBlend(img, lx, ly, 0xFFFF8800, Math.abs(distortion) * 0.05f);
                }
            }
        }

        // Rising ember particles
        if (!particlesInited) {
            Random r = new Random(321);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = FH - 1 - r.nextFloat() * 8;
                pDY[i] = -(0.15f + r.nextFloat() * 0.25f);
                pDX[i] = (r.nextFloat() - 0.5f) * 0.1f;
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 1 + r.nextFloat() * 2;
                pLife[i] = 1.0f;
            }
            particlesInited = true;
        }

        for (int i = 0; i < P_COUNT; i++) {
            pY[i] += pDY[i];
            pX[i] += pDX[i] + (float) Math.sin(tickCounter * 0.08 + pPhase[i]) * 0.2f;
            pLife[i] -= 0.005f;

            if (pY[i] < -3 || pLife[i] <= 0) {
                pY[i] = FH - 1 - new Random().nextFloat() * 5;
                pX[i] = new Random().nextFloat() * FW;
                pLife[i] = 1.0f;
            }

            float yNorm = pY[i] / FH;
            int lx = (int) pX[i] % FW, ly = (int) pY[i];
            if (lx < 0) lx += FW;

            // Ember color fades from bright to dim as it rises
            float bright = yNorm * pLife[i];
            int er = 255, eg = (int)(220 * bright), eb = (int)(50 * bright * bright);
            float sz = pSize[i];

            // Draw ember with glow
            for (int dy = (int)-sz; dy <= (int) sz; dy++) {
                for (int dx = (int)-sz; dx <= (int) sz; dx++) {
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d <= sz) {
                        float glow = (1 - d / sz) * bright * 0.7f;
                        faceBlend(img, lx + dx, ly + dy, argb(255, er, eg, eb), glow);
                    }
                }
            }
        }

        // Bright flickering core at bottom
        float flicker = (float)(Math.sin(tickCounter * 0.3) * 0.5 + 0.5) * 0.15f;
        for (int ly = FH - 12; ly < FH; ly++) {
            float s = (float)(ly - (FH - 12)) / 12.0f;
            for (int lx = 0; lx < FW; lx++) {
                float wave = (float)(Math.sin(lx * 0.15 + tickCounter * 0.2) * 0.5 + 0.5);
                faceBlend(img, lx, ly, 0xFFFFDD44, s * wave * flicker);
            }
        }
    }

    // ========== OCEAN - wave motion with depth + foam ==========
    private static void animateOcean(NativeImage img) {
        float time = tickCounter * 0.04f;

        // Multi-layer wave animation
        for (int layer = 0; layer < 5; layer++) {
            float speed = 0.05f + layer * 0.015f;
            float offset = tickCounter * speed + layer * 2.5f;
            float freq = 0.08f + layer * 0.015f;
            float amp = 3.0f - layer * 0.4f;
            int baseY = 6 + layer * 24;
            int bright = 195 - layer * 25;

            for (int lx = 0; lx < FW; lx++) {
                // Compound wave motion
                float wave1 = (float) Math.sin(lx * freq + offset);
                float wave2 = (float) Math.sin(lx * freq * 1.7f + offset * 0.8f + 1.2f) * 0.5f;
                int ly = baseY + (int)((wave1 + wave2) * amp);

                // Wave crest (bright line)
                faceBlend(img, lx, ly, argb(255, bright + 20, bright + 30, 255), 0.35f);
                faceBlend(img, lx, ly + 1, argb(255, bright, bright + 10, 250), 0.25f);
                faceBlend(img, lx, ly + 2, argb(255, bright - 20, bright - 10, 240), 0.12f);

                // Foam that appears and dissolves
                float foamChance = (float)(Math.sin(lx * 0.3 + offset * 2) * 0.5 + 0.5);
                if (foamChance > 0.65f && (lx + (int)(offset * 3)) % 3 == 0) {
                    float foamAlpha = (foamChance - 0.65f) / 0.35f * 0.55f;
                    faceBlend(img, lx, ly - 1, 0xFFE8F4FF, foamAlpha);
                    faceBlend(img, lx + 1, ly - 1, 0xFFDDEEFF, foamAlpha * 0.6f);
                    faceBlend(img, lx - 1, ly - 1, 0xFFDDEEFF, foamAlpha * 0.5f);
                    // Foam dissolve dots
                    if (foamChance > 0.8f) {
                        faceBlend(img, lx, ly - 2, 0xFFD0E8FF, foamAlpha * 0.3f);
                    }
                }
            }
        }

        // Deep water color shift (subtle pulsing)
        float depthPulse = (float)(Math.sin(tickCounter * 0.025) + 1) * 0.025f;
        for (int ly = FH / 3; ly < FH; ly++) {
            float depth = (float)(ly - FH / 3) / (FH * 2f / 3);
            for (int lx = 0; lx < FW; lx += 2) {
                faceBlend(img, lx, ly, 0xFF0840A0, depthPulse * depth);
            }
        }

        // Caustic light animation (shifting light patterns underwater)
        float causticTime = tickCounter * 0.06f;
        for (int ly = FH / 4; ly < FH * 3 / 4; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float c1 = (float)(Math.sin(lx * 0.18 + causticTime) * Math.sin(ly * 0.12 - causticTime * 0.7));
                float c2 = (float)(Math.sin(lx * 0.1 - causticTime * 0.5) * Math.cos(ly * 0.15 + causticTime * 0.3));
                float caustic = (c1 + c2) * 0.5f;
                if (caustic > 0.4f) {
                    faceBlend(img, lx, ly, 0xFF4090D0, (caustic - 0.4f) * 0.15f);
                }
            }
        }

        // Surface light sparkles
        for (int i = 0; i < 8; i++) {
            float sparkleX = (pseudoRand(i * 7, (int)(time * 3)) * FW);
            float sparkleY = pseudoRand(i * 13, (int)(time * 2)) * FH * 0.3f;
            float bright = (float)(Math.sin(tickCounter * 0.1 + i * 1.5) * 0.5 + 0.5);
            if (bright > 0.6f) {
                faceBlend(img, (int) sparkleX, (int) sparkleY, 0xFFEEF8FF, (bright - 0.6f) * 0.8f);
            }
        }
    }

    // ========== EMERALD - rotating light reflection across facets ==========
    private static void animateEmerald(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(654);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 1 + r.nextFloat() * 2;
            }
            particlesInited = true;
        }

        // Rotating light band that sweeps across the cape (like light moving across a gem)
        float sweepAngle = tickCounter * 0.02f;
        float sweepDirX = (float) Math.cos(sweepAngle);
        float sweepDirY = (float) Math.sin(sweepAngle);
        float sweepPos = (tickCounter * 0.4f) % (FW + FH + 20) - 10;

        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float proj = lx * sweepDirX + ly * sweepDirY;
                float dist = Math.abs(proj - sweepPos);
                if (dist < 8) {
                    float intensity = (float)(Math.cos(dist / 8 * Math.PI) + 1) * 0.1f;
                    faceBlend(img, lx, ly, 0xFFCCFFDD, intensity);
                }
            }
        }

        // Secondary cross sweep (perpendicular, slower)
        float sweep2Pos = (tickCounter * 0.25f) % (FW + FH + 20) - 10;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float proj = lx * (-sweepDirY) + ly * sweepDirX;
                float dist = Math.abs(proj - sweep2Pos);
                if (dist < 5) {
                    float intensity = (float)(Math.cos(dist / 5 * Math.PI) + 1) * 0.06f;
                    faceBlend(img, lx, ly, 0xFFAAFFBB, intensity);
                }
            }
        }

        // Sharp gem glints (flash brightly then fade)
        for (int i = 0; i < P_COUNT; i++) {
            float flash = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.06 + pPhase[i])), 8);
            int lx = (int) pX[i], ly = (int) pY[i];
            if (flash > 0.03f) {
                float sz = pSize[i] * (1 + flash * 2);
                // Bright center
                faceBlend(img, lx, ly, 0xFFBBFFDD, flash * 0.9f);
                // Cross rays
                for (int f = 1; f <= (int) sz; f++) {
                    float fade = 1.0f - f / (sz + 1);
                    faceBlend(img, lx + f, ly, 0xFF88DDAA, flash * fade * 0.4f);
                    faceBlend(img, lx - f, ly, 0xFF88DDAA, flash * fade * 0.4f);
                    faceBlend(img, lx, ly + f, 0xFF88DDAA, flash * fade * 0.4f);
                    faceBlend(img, lx, ly - f, 0xFF88DDAA, flash * fade * 0.4f);
                }
            }
        }

        // Edge glow pulse (neon green border breathing)
        float glowPulse = (float)(Math.sin(tickCounter * 0.04) * 0.5 + 0.5) * 0.15f + 0.05f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float edgeDist = Math.min(Math.min(lx, FW - 1 - lx), Math.min(ly, FH - 1 - ly)) / 6.0f;
                if (edgeDist < 1.0f) {
                    faceBlend(img, lx, ly, 0xFF55FF88, (1 - edgeDist) * glowPulse);
                }
            }
        }
    }

    // ========== SUNSET - shifting golden hour colors + drifting rays ==========
    private static void animateSunset(NativeImage img) {
        // Slow hue shift through golden palette
        float hueShift = (float)(Math.sin(tickCounter * 0.012) * 0.1);
        float warmShift = (float)(Math.sin(tickCounter * 0.008 + 1) * 0.06);

        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float warmth = (float)(Math.sin(lx * 0.08 + ly * 0.04 + tickCounter * 0.02)) * 0.04f;
                float total = Math.max(0, hueShift + warmShift + warmth);
                if (total > 0.01f) {
                    // Warm golden overlay
                    faceBlend(img, lx, ly, 0xFFFFCC55, total);
                }
            }
        }

        // Animated sun rays (rotating slowly)
        float rayAngleOffset = tickCounter * 0.008f;
        int sunCx = 40, sunCy = 25;
        for (int ray = 0; ray < 12; ray++) {
            double angle = ray * Math.PI / 6.0 + rayAngleOffset;
            float rayLen = 20 + (float) Math.sin(tickCounter * 0.05 + ray * 0.8) * 6;
            float rayWidth = 2.0f + (float) Math.sin(tickCounter * 0.03 + ray * 1.2) * 0.8f;
            float rayBrightness = 0.15f + (float)(Math.sin(tickCounter * 0.04 + ray * 0.5) * 0.5 + 0.5) * 0.1f;

            for (int i = 17; i < 17 + (int) rayLen; i++) {
                float fade = 1.0f - (float)(i - 17) / rayLen;
                fade = fade * fade;
                int px = sunCx + (int)(Math.cos(angle) * i);
                int py = sunCy + (int)(Math.sin(angle) * i);
                for (int w = (int)-rayWidth; w <= (int) rayWidth; w++) {
                    float wFade = 1.0f - (float) Math.abs(w) / rayWidth;
                    int wx = px + (int)(Math.cos(angle + Math.PI / 2) * w);
                    int wy = py + (int)(Math.sin(angle + Math.PI / 2) * w);
                    faceBlend(img, wx, wy, 0xFFFFEEAA, fade * wFade * rayBrightness);
                }
            }
        }

        // Cloud movement (subtle horizontal drift)
        float cloudDrift = (float) Math.sin(tickCounter * 0.01) * 0.8f;
        // Light rays between clouds
        for (int lx = 0; lx < FW; lx++) {
            float rayIntensity = (float)(Math.sin((lx + cloudDrift * 3) * 0.15 + tickCounter * 0.015) * 0.5 + 0.5);
            rayIntensity = (float) Math.pow(rayIntensity, 4) * 0.12f;
            for (int ly = 30; ly < FH; ly++) {
                float yFade = 1.0f - (float)(ly - 30) / (FH - 30);
                faceBlend(img, lx, ly, 0xFFFFDDAA, rayIntensity * yFade);
            }
        }

        // Sun corona pulse
        float coronaPulse = (float)(Math.sin(tickCounter * 0.05) * 0.5 + 0.5);
        for (int dy = -22; dy <= 22; dy++) {
            for (int dx = -22; dx <= 22; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > 16 && d < 22) {
                    float glow = (1 - (d - 16) / 6.0f) * coronaPulse * 0.12f;
                    faceBlend(img, sunCx + dx, sunCy + dy, 0xFFFFEE88, glow);
                }
            }
        }
    }

    // ========== GALAXY - rotating spiral + nebula clouds + star orbits ==========
    private static void animateGalaxy(NativeImage img) {
        float cx = FW / 2.0f, cy = FH / 2.0f;
        float rotSpeed = tickCounter * 0.008f;

        // Slowly rotating spiral arm overlay
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dx = lx - cx, dy = (ly - cy) * 0.6f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);

                // Rotate with time
                float swirl = (float)(Math.sin(angle * 2 + dist * 0.08 + rotSpeed) * 0.5 + 0.5);
                float falloff = Math.max(0, 1.0f - dist / 35.0f);
                float intensity = swirl * falloff * falloff * 0.1f;

                if (intensity > 0.01f) {
                    // Spiral arm color (blue-purple)
                    faceBlend(img, lx, ly, 0xFF5520BB, intensity);
                }
            }
        }

        // Nebula cloud pulsation
        float nebulaPulse = (float)(Math.sin(tickCounter * 0.02) * 0.5 + 0.5);
        // Animated nebula patches
        animateNebulaPatch(img, 15, 30, 12, argb(255, 90, 30, 140), nebulaPulse);
        animateNebulaPatch(img, 60, 25, 10, argb(255, 35, 55, 145), 1 - nebulaPulse);
        animateNebulaPatch(img, 25, 90, 14, argb(255, 110, 30, 90), nebulaPulse * 0.7f);

        // Twinkling stars
        if (!particlesInited) {
            Random r = new Random(999);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 1 + r.nextFloat() * 3;
            }
            // Orbiting star cluster particles
            for (int i = 0; i < S_COUNT; i++) {
                sPhase[i] = new Random().nextFloat() * 6.28f;
                float orbitR = 15 + new Random().nextFloat() * 20;
                sX[i] = orbitR; // Store orbit radius
                sLife[i] = new Random().nextFloat();
            }
            particlesInited = true;
        }

        // Star twinkle with cross flares
        for (int i = 0; i < P_COUNT; i++) {
            float twinkle = (float)(Math.sin(tickCounter * 0.07 + pPhase[i]) * 0.5 + 0.5);
            twinkle = twinkle * twinkle;
            int lx = (int) pX[i], ly = (int) pY[i];

            faceBlend(img, lx, ly, 0xFFEEEEFF, twinkle * 0.75f);
            if (twinkle > 0.5f && pSize[i] > 2) {
                float flare = (twinkle - 0.5f) * 2;
                for (int f = 1; f <= (int)(pSize[i] * flare); f++) {
                    float fade = 1.0f - f / (pSize[i] * flare + 1);
                    faceBlend(img, lx + f, ly, 0xFFCCCCFF, fade * flare * 0.3f);
                    faceBlend(img, lx - f, ly, 0xFFCCCCFF, fade * flare * 0.3f);
                    faceBlend(img, lx, ly + f, 0xFFCCCCFF, fade * flare * 0.3f);
                    faceBlend(img, lx, ly - f, 0xFFCCCCFF, fade * flare * 0.3f);
                }
            }
        }

        // Orbiting star clusters (small dots orbiting the center)
        for (int i = 0; i < S_COUNT; i++) {
            float orbitR = sX[i];
            float orbitAngle = rotSpeed * (0.5f + sLife[i] * 0.5f) + sPhase[i];
            int ox = (int)(cx + Math.cos(orbitAngle) * orbitR);
            int oy = (int)(cy + Math.sin(orbitAngle) * orbitR * 0.6f); // Elliptical orbit
            float bright = 0.4f + (float)(Math.sin(tickCounter * 0.05 + sPhase[i]) * 0.3);
            faceBlend(img, ox, oy, 0xFFDDDDFF, bright);
            // Trail
            for (int t = 1; t <= 3; t++) {
                float trailAngle = orbitAngle - t * 0.05f;
                int tx = (int)(cx + Math.cos(trailAngle) * orbitR);
                int ty = (int)(cy + Math.sin(trailAngle) * orbitR * 0.6f);
                faceBlend(img, tx, ty, 0xFFAAAADD, bright * (1 - t / 4.0f) * 0.5f);
            }
        }

        // Core glow pulsation
        float corePulse = (float)(Math.sin(tickCounter * 0.03) * 0.5 + 0.5);
        for (int dy = -6; dy <= 6; dy++) {
            for (int dx = -8; dx <= 8; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy * 1.5f);
                if (d < 6) {
                    float glow = (1 - d / 6.0f) * corePulse * 0.2f;
                    faceBlend(img, (int) cx + dx, (int) cy + dy, 0xFFCCBBEE, glow);
                }
            }
        }
    }

    private static void animateNebulaPatch(NativeImage img, int ncx, int ncy, int radius, int color, float pulse) {
        for (int dy = -radius; dy <= radius; dy += 2) {
            for (int dx = -radius; dx <= radius; dx += 2) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < radius) {
                    float alpha = (1 - d / radius) * pulse * 0.08f;
                    if (alpha > 0.005f) {
                        faceBlend(img, ncx + dx, ncy + dy, color, alpha);
                        faceBlend(img, ncx + dx + 1, ncy + dy, color, alpha * 0.7f);
                        faceBlend(img, ncx + dx, ncy + dy + 1, color, alpha * 0.7f);
                    }
                }
            }
        }
    }
}
