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
    private static final Identifier ANIMATED_CAPE_ID = Identifier.of("pulsar-core", "animated_cape_dynamic");
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
            String path = "/assets/pulsar-core/textures/cape/" + capeFile;
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
            dynamicTexture = new NativeImageBackedTexture(() -> "pulsar-core:animated_cape_dynamic", target);
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
            case "void_cape.png" -> animateVoid(image);
            case "lightning_cape.png" -> animateLightning(image);
            case "blood_cape.png" -> animateBlood(image);
            case "arctic_cape.png" -> animateArctic(image);
            case "phantom_cape.png" -> animatePhantom(image);
            case "neon_cape.png" -> animateNeon(image);
            case "lava_cape.png" -> animateLava(image);
            case "sakura_cape.png" -> animateSakura(image);
            case "storm_cape.png" -> animateStorm(image);
            case "solar_cape.png" -> animateSolar(image);
            case "amethyst_cape.png" -> animateAmethyst(image);
            case "inferno_cape.png" -> animateInferno(image);
            case "drift_cape.png" -> animateDrift(image);
            case "obsidian_cape.png" -> animateObsidian(image);
            case "blackhole_cape.png" -> animateBlackHole(image);
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

    // ========== VOID — dark energy swirl ==========
    private static void animateVoid(NativeImage img) {
        float cx = FW / 2.0f, cy = FH / 2.0f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dx = lx - cx, dy = ly - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);
                float swirl = (float)(Math.sin(angle * 3 + dist * 0.1 - tickCounter * 0.05) * 0.5 + 0.5);
                swirl *= Math.max(0, 1.0f - dist / 45.0f) * 0.2f;
                faceBlend(img, lx, ly, 0xFFA050FF, swirl);
            }
        }
        // Pulsing dark core
        float pulse = (float)(Math.sin(tickCounter * 0.06) + 1) * 0.08f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dx = lx - cx, dy = ly - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 15) {
                    faceBlend(img, lx, ly, 0xFF000000, (1.0f - dist / 15) * pulse);
                }
            }
        }
    }

    // ========== LIGHTNING — electric flashes ==========
    private static void animateLightning(NativeImage img) {
        // Flickering brightness
        float flicker = (float)(Math.sin(tickCounter * 0.3) * Math.cos(tickCounter * 0.17)) * 0.1f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                faceBlend(img, lx, ly, 0xFFFFFF00, Math.max(0, flicker));
            }
        }
        // Random lightning flash every ~2 seconds
        if (tickCounter % 40 < 3) {
            int boltX = 10 + (tickCounter * 7) % (FW - 20);
            int bx = boltX;
            for (int by = 0; by < FH; by += 3) {
                bx += (int)((Math.sin(by * 0.5 + tickCounter) - 0.5) * 6);
                bx = Math.max(2, Math.min(FW - 3, bx));
                faceBlend(img, bx, by, 0xFFFFFFFF, 0.9f);
                faceBlend(img, bx - 1, by, 0xFFFFFFDD, 0.5f);
                faceBlend(img, bx + 1, by, 0xFFFFFFDD, 0.5f);
                faceBlend(img, bx, by + 1, 0xFFFFFFCC, 0.6f);
                faceBlend(img, bx, by + 2, 0xFFFFFFAA, 0.3f);
            }
        }
        // Ambient sparkles
        if (!particlesInited) {
            Random r = new Random(777);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
            }
            particlesInited = true;
        }
        for (int i = 0; i < P_COUNT; i++) {
            float flash = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.15 + pPhase[i])), 8);
            if (flash > 0.1f) {
                faceBlend(img, (int) pX[i], (int) pY[i], 0xFFFFFFEE, flash * 0.7f);
            }
        }
    }

    // ========== BLOODMOON — pulsing red glow ==========
    private static void animateBlood(NativeImage img) {
        // Pulsing blood red
        float pulse = (float)(Math.sin(tickCounter * 0.04) + 1) * 0.06f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                faceBlend(img, lx, ly, 0xFFCC0000, pulse);
            }
        }
        // Moon glow pulse
        int moonCx = FW * 2 / 3, moonCy = FH / 3;
        float moonPulse = (float)(Math.sin(tickCounter * 0.05) + 1) * 0.15f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dx = lx - moonCx, dy = ly - moonCy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 18) {
                    faceBlend(img, lx, ly, 0xFFFF3030, (1.0f - dist / 18) * moonPulse);
                }
            }
        }
        // Dripping blood particles
        for (int i = 0; i < 5; i++) {
            int dx = 10 + i * 15;
            int dy = (int)((tickCounter * 0.5 + i * 20) % FH);
            faceBlend(img, dx, dy, 0xFFAA0000, 0.5f);
            faceBlend(img, dx, dy + 1, 0xFF880000, 0.3f);
        }
    }

    // ========== ARCTIC — shimmering aurora ==========
    private static void animateArctic(NativeImage img) {
        // Flowing aurora bands
        for (int band = 0; band < 4; band++) {
            float speed = 0.04f + band * 0.01f;
            float offset = tickCounter * speed + band * 1.5f;
            int baseY = 12 + band * 16;
            for (int lx = 0; lx < FW; lx++) {
                int ly = baseY + (int)(Math.sin(lx * 0.12 + offset) * 4);
                int r = band % 2 == 0 ? 100 : 80;
                int g = band % 2 == 0 ? 255 : 200;
                int b = band % 2 == 0 ? 180 : 255;
                faceBlend(img, lx, ly, (0xFF << 24) | (r << 16) | (g << 8) | b, 0.15f);
                faceBlend(img, lx, ly + 1, (0xFF << 24) | (r << 16) | (g << 8) | b, 0.08f);
            }
        }
        // Ice sparkle flashes
        if (!particlesInited) {
            Random r = new Random(555);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
            }
            particlesInited = true;
        }
        for (int i = 0; i < P_COUNT; i++) {
            float flash = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.07 + pPhase[i])), 5);
            if (flash > 0.1f) {
                faceBlend(img, (int) pX[i], (int) pY[i], 0xFFEEF8FF, flash * 0.8f);
                faceBlend(img, (int) pX[i] + 1, (int) pY[i], 0xFFDDEEFF, flash * 0.3f);
                faceBlend(img, (int) pX[i] - 1, (int) pY[i], 0xFFDDEEFF, flash * 0.3f);
            }
        }
        // Gentle shimmer
        float shimmer = (tickCounter * 0.7f) % (FW + FH + 8) - 4;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - shimmer);
                if (dist < 4) {
                    float intensity = (float)(Math.cos(dist / 4 * Math.PI) + 1) * 0.08f;
                    faceBlend(img, lx, ly, 0xFFE0F0FF, intensity);
                }
            }
        }
    }

    // ========== PHANTOM - drifting spectral wisps + ghostly pulse ==========
    private static void animatePhantom(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(777);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = r.nextFloat() * FH;
                pDX[i] = (r.nextFloat() - 0.5f) * 0.12f;
                pDY[i] = -(0.05f + r.nextFloat() * 0.1f); // Drift upward
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 4 + r.nextFloat() * 6;
                pLife[i] = 0.3f + r.nextFloat() * 0.5f;
            }
            particlesInited = true;
        }

        // Ghost breathing pulse (whole cape brightens/dims)
        float breathe = (float)(Math.sin(tickCounter * 0.025) * 0.04);
        if (Math.abs(breathe) > 0.005f) {
            for (int ly = 0; ly < FH; ly += 2) {
                for (int lx = 0; lx < FW; lx += 2) {
                    faceBlend(img, lx, ly, breathe > 0 ? 0xFFEEEEFF : 0xFFCCCCDD, Math.abs(breathe));
                }
            }
        }

        // Drifting wisp particles
        for (int i = 0; i < P_COUNT; i++) {
            float windX = (float) Math.sin(tickCounter * 0.03 + pPhase[i]) * 0.2f;
            float windY = (float) Math.cos(tickCounter * 0.02 + pPhase[i] * 0.7) * 0.05f;
            pX[i] += pDX[i] + windX;
            pY[i] += pDY[i] + windY;

            if (pY[i] < -pSize[i] * 2) {
                pY[i] = FH + pSize[i];
                pX[i] = new Random().nextFloat() * FW;
            }
            if (pX[i] < -5) pX[i] += FW + 10;
            if (pX[i] >= FW + 5) pX[i] -= FW + 10;

            int px = (int) pX[i], py = (int) pY[i];
            float sz = pSize[i];
            float alpha = pLife[i] * (0.25f + (float) Math.sin(tickCounter * 0.04 + pPhase[i]) * 0.1f);

            // Elongated wisp shape
            for (int dy = (int)(-sz * 1.5f); dy <= (int)(sz * 1.5f); dy++) {
                for (int dx = (int)(-sz * 0.6f); dx <= (int)(sz * 0.6f); dx++) {
                    float ex = dx / (sz * 0.6f), ey = dy / (sz * 1.5f);
                    float dist = ex * ex + ey * ey;
                    if (dist <= 1.0f) {
                        float pa = alpha * (1 - dist * 0.6f);
                        faceBlend(img, px + dx, py + dy, 0xFFE8E8FF, pa);
                    }
                }
            }
        }

        // Ghostly shimmer sweep
        float shimmer = (tickCounter * 0.4f) % (FW + FH + 20) - 10;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - shimmer);
                if (dist < 8) {
                    float intensity = (float)(Math.cos(dist / 8 * Math.PI) + 1) * 0.06f;
                    faceBlend(img, lx, ly, 0xFFFFFFFF, intensity);
                }
            }
        }
    }

    // ========== NEON - glitch flicker + scanline scroll + neon pulse ==========
    private static void animateNeon(NativeImage img) {
        // Scrolling scanlines
        int scanOffset = (tickCounter * 2) % FH;
        for (int ly = 0; ly < FH; ly++) {
            int shifted = (ly + scanOffset) % FH;
            if (shifted % 3 == 0) {
                for (int lx = 0; lx < FW; lx++) {
                    faceBlend(img, lx, ly, 0xFF000000, 0.06f);
                }
            }
        }

        // Neon color pulsing (pink and cyan alternate emphasis)
        float pinkPulse = (float)(Math.sin(tickCounter * 0.06) * 0.5 + 0.5);
        float cyanPulse = (float)(Math.sin(tickCounter * 0.06 + Math.PI) * 0.5 + 0.5);
        for (int ly = 0; ly < FH; ly += 3) {
            for (int lx = 0; lx < FW; lx += 3) {
                faceBlend(img, lx, ly, 0xFFFF32C8, pinkPulse * 0.06f);
                faceBlend(img, lx + 1, ly + 1, 0xFF00FFFF, cyanPulse * 0.06f);
            }
        }

        // Glitch blocks (random rectangles that flash)
        if (tickCounter % 8 < 2) {
            int glitchY = (int)(pseudoRand(tickCounter / 8, 33) * FH);
            int glitchH = 2 + (int)(pseudoRand(tickCounter / 8 + 1, 44) * 5);
            int glitchColor = tickCounter % 16 < 8 ? 0xFFFF40D0 : 0xFF00EEFF;
            for (int ly = glitchY; ly < Math.min(FH, glitchY + glitchH); ly++) {
                int offset = (int)(pseudoRand(ly + tickCounter, 55) * 8) - 4;
                for (int lx = 0; lx < FW; lx++) {
                    int shifted = (lx + offset) % FW;
                    if (shifted < 0) shifted += FW;
                    faceBlend(img, shifted, ly, glitchColor, 0.25f);
                }
            }
        }

        // Diagonal neon sweep
        float sweep = (tickCounter * 0.8f) % (FW + FH + 16) - 8;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - sweep);
                if (dist < 4) {
                    float intensity = (float)(Math.cos(dist / 4 * Math.PI) + 1) * 0.12f;
                    int color = tickCounter % 40 < 20 ? 0xFFFF60E0 : 0xFF40FFFF;
                    faceBlend(img, lx, ly, color, intensity);
                }
            }
        }
    }

    // ========== LAVA - flowing molten lava + cooling crust ==========
    private static void animateLava(NativeImage img) {
        float time = tickCounter * 0.05f;

        // Flowing lava (scrolling noise patterns)
        for (int ly = 0; ly < FH; ly++) {
            float yFactor = (float) ly / FH;
            for (int lx = 0; lx < FW; lx++) {
                float n1 = noise2D(lx * 0.05f, ly * 0.04f - time);
                float n2 = noise2D(lx * 0.1f + 40, ly * 0.07f - time * 1.5f);
                float flow = (n1 * 0.6f + n2 * 0.4f) * yFactor;
                if (flow > 0.2f) {
                    float intensity = (flow - 0.2f) * 0.4f;
                    int fr = 255, fg = (int)(160 * intensity / 0.3f), fb = (int)(20 * intensity / 0.3f);
                    faceBlend(img, lx, ly, argb(255, fr, Math.min(255, fg), fb), Math.min(0.3f, intensity));
                }
            }
        }

        // Bubbling lava pools
        if (!particlesInited) {
            Random r = new Random(888);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = r.nextFloat() * FH;
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 2 + r.nextFloat() * 4;
            }
            particlesInited = true;
        }

        // Lava bubbles (pulsing circles)
        for (int i = 0; i < P_COUNT; i++) {
            float bubble = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.04 + pPhase[i])), 3);
            if (bubble > 0.1f) {
                int bx = (int) pX[i], by = (int) pY[i];
                float sz = pSize[i] * bubble;
                for (int dy = (int)-sz; dy <= (int) sz; dy++) {
                    for (int dx = (int)-sz; dx <= (int) sz; dx++) {
                        float d = (float) Math.sqrt(dx * dx + dy * dy);
                        if (d <= sz) {
                            float glow = (1 - d / sz) * bubble * 0.6f;
                            faceBlend(img, bx + dx, by + dy, 0xFFFF9020, glow);
                        }
                    }
                }
            }
        }

        // Heat shimmer at top
        float heatTime = tickCounter * 0.1f;
        for (int ly = 0; ly < FH / 3; ly++) {
            float distortion = (float)(Math.sin(ly * 0.25 + heatTime) * 0.5);
            if (Math.abs(distortion) > 0.2f) {
                for (int lx = 0; lx < FW; lx++) {
                    faceBlend(img, lx, ly, 0xFFCC6600, Math.abs(distortion) * 0.05f);
                }
            }
        }
    }

    // ========== SAKURA - falling cherry blossom petals ==========
    private static void animateSakura(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(123);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = -r.nextFloat() * FH;
                pDX[i] = (r.nextFloat() - 0.5f) * 0.12f;
                pDY[i] = 0.1f + r.nextFloat() * 0.15f;
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 3 + r.nextFloat() * 4;
                pRot[i] = r.nextFloat() * 6.28f;
                pLife[i] = 0.5f + r.nextFloat() * 0.4f;
            }
            particlesInited = true;
        }

        for (int i = 0; i < P_COUNT; i++) {
            float windX = (float) Math.sin(tickCounter * 0.035 + pPhase[i]) * 0.2f;
            float windY = (float) Math.cos(tickCounter * 0.02 + pPhase[i] * 0.7) * 0.04f;
            pX[i] += pDX[i] + windX;
            pY[i] += pDY[i] + windY;
            pRot[i] += 0.025f + (float) Math.sin(tickCounter * 0.04 + pPhase[i]) * 0.015f;

            if (pY[i] >= FH + 5) {
                pY[i] = -pSize[i] * 2;
                pX[i] = new Random().nextFloat() * FW;
            }
            if (pX[i] < -5) pX[i] += FW + 10;
            if (pX[i] >= FW + 5) pX[i] -= FW + 10;

            int px = (int) pX[i], py = (int) pY[i];
            float sz = pSize[i];
            float cos = (float) Math.cos(pRot[i]), sin = (float) Math.sin(pRot[i]);
            float alpha = pLife[i] * (0.4f + (float) Math.sin(tickCounter * 0.05 + pPhase[i]) * 0.1f);

            for (int dy = (int)(-sz - 1); dy <= (int)(sz + 1); dy++) {
                for (int dx = (int)(-sz - 1); dx <= (int)(sz + 1); dx++) {
                    float lx = dx * cos - dy * sin;
                    float ly = dx * sin + dy * cos;
                    float ex = lx / sz, ey = ly / (sz * 0.45f);
                    float dist = ex * ex + ey * ey;
                    if (dist <= 1.0f) {
                        float inner = 1 - dist;
                        int r = 255, g = 200 + (int)(inner * 35), b = 215 + (int)(inner * 25);
                        faceBlend(img, px + dx, py + dy, argb(255, r, g, b), alpha * (1 - dist * 0.4f));
                    }
                }
            }
        }

        // Gentle warm shimmer
        float shimmer = (tickCounter * 0.5f) % (FW + FH + 16) - 8;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - shimmer);
                if (dist < 5) {
                    float intensity = (float)(Math.cos(dist / 5 * Math.PI) + 1) * 0.06f;
                    faceBlend(img, lx, ly, 0xFFFFEEF0, intensity);
                }
            }
        }
    }

    // ========== STORM - lightning flashes + rain animation ==========
    private static void animateStorm(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(999);
            // Rain drops
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = r.nextFloat() * FH;
                pDY[i] = 1.5f + r.nextFloat() * 1.5f;
                pDX[i] = -0.3f; // Slight wind
                pSize[i] = 3 + r.nextFloat() * 5; // Streak length
                pPhase[i] = r.nextFloat();
            }
            sLife[0] = 0; // Lightning flash timer
            particlesInited = true;
        }

        // Rain animation
        for (int i = 0; i < P_COUNT; i++) {
            pY[i] += pDY[i];
            pX[i] += pDX[i];
            if (pY[i] >= FH) { pY[i] = -pSize[i]; pX[i] = new Random().nextFloat() * FW; }
            if (pX[i] < 0) pX[i] += FW;

            int rx = (int) pX[i], ry = (int) pY[i];
            int len = (int) pSize[i];
            for (int j = 0; j < len; j++) {
                float fade = 1 - (float) j / len;
                faceBlend(img, rx, ry + j, 0xFFA0B8D0, fade * 0.35f * pPhase[i]);
            }
        }

        // Lightning flash (occasional full-cape flash)
        sLife[0] -= 0.04f;
        if (sLife[0] <= 0 && tickCounter % 90 < 2) {
            sLife[0] = 1.0f;
        }
        if (sLife[0] > 0) {
            float flash = sLife[0] * sLife[0]; // Quick decay
            for (int ly = 0; ly < FH; ly += 2) {
                for (int lx = 0; lx < FW; lx += 2) {
                    faceBlend(img, lx, ly, 0xFFFFFFDD, flash * 0.3f);
                }
            }
            // Bright bolt center
            if (sLife[0] > 0.5f) {
                int boltX = 30 + (int)(pseudoRand(tickCounter / 90, 33) * 20);
                for (int ly = 0; ly < FH; ly++) {
                    int jitter = (int)(pseudoRand(ly + tickCounter, 11) * 6) - 3;
                    faceBlend(img, boltX + jitter, ly, 0xFFFFFFFF, sLife[0] * 0.6f);
                    faceBlend(img, boltX + jitter + 1, ly, 0xFFEEEEFF, sLife[0] * 0.3f);
                }
            }
        }

        // Rolling cloud darkness
        float cloudPhase = tickCounter * 0.02f;
        for (int ly = 0; ly < FH / 3; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float n = noise2D(lx * 0.06f + cloudPhase, ly * 0.08f);
                if (n > 0.5f) {
                    faceBlend(img, lx, ly, 0xFF1A1C22, (n - 0.5f) * 0.3f);
                }
            }
        }
    }

    // ========== SOLAR - pulsing corona + rotating solar flares ==========
    private static void animateSolar(NativeImage img) {
        int scx = FW / 2, scy = 28;

        // Pulsing corona
        float pulse = (float)(Math.sin(tickCounter * 0.04) * 0.5 + 0.5);
        int coronaRadius = 18 + (int)(pulse * 6);
        for (int dy = -coronaRadius; dy <= coronaRadius; dy++) {
            for (int dx = -coronaRadius; dx <= coronaRadius; dx++) {
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > 14 && d <= coronaRadius) {
                    float glow = (1 - (d - 14) / (coronaRadius - 14)) * pulse * 0.25f;
                    faceBlend(img, scx + dx, scy + dy, 0xFFFFE060, glow);
                }
            }
        }

        // Rotating solar flare rays
        float rotAngle = tickCounter * 0.015f;
        for (int ray = 0; ray < 8; ray++) {
            double angle = rotAngle + ray * Math.PI / 4;
            float rayLen = 35 + 10 * (float) Math.sin(tickCounter * 0.05 + ray * 1.5);
            for (int r = 15; r <= (int) rayLen; r++) {
                float t = (float)(r - 15) / (rayLen - 15);
                int px = scx + (int)(Math.cos(angle) * r);
                int py = scy + (int)(Math.sin(angle) * r);
                float alpha = (1 - t) * 0.2f * (0.7f + 0.3f * (float) Math.sin(tickCounter * 0.08 + ray));
                faceBlend(img, px, py, 0xFFFFDD50, alpha);
                faceBlend(img, px + 1, py, 0xFFFFCC30, alpha * 0.5f);
                faceBlend(img, px, py + 1, 0xFFFFCC30, alpha * 0.5f);
            }
        }

        // Solar prominences (animated arcs)
        float arcPhase = tickCounter * 0.03f;
        for (int arc = 0; arc < 3; arc++) {
            float baseAngle = arcPhase + arc * 2.1f;
            int startR = 14;
            for (int step = 0; step < 20; step++) {
                float t = (float) step / 20;
                float angle = baseAngle + t * 0.8f;
                float r = startR + (float) Math.sin(t * Math.PI) * 12;
                int px = scx + (int)(Math.cos(angle) * r);
                int py = scy + (int)(Math.sin(angle) * r);
                float alpha = (float) Math.sin(t * Math.PI) * 0.3f;
                faceBlend(img, px, py, 0xFFFFAA30, alpha);
                faceBlend(img, px + 1, py, 0xFFFF8820, alpha * 0.5f);
            }
        }

        // Heat wave distortion below
        for (int ly = FH / 2; ly < FH; ly++) {
            float wave = (float)(Math.sin(ly * 0.15 + tickCounter * 0.08) * 0.5 + 0.5);
            for (int lx = 0; lx < FW; lx++) {
                faceBlend(img, lx, ly, 0xFFFF6600, wave * 0.03f);
            }
        }
    }

    // ========== AMETHYST - crystal shimmer + light refraction ==========
    private static void animateAmethyst(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(333);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 1 + r.nextFloat() * 3;
            }
            particlesInited = true;
        }

        // Rotating light refraction across crystal facets
        float sweepAngle = tickCounter * 0.018f;
        float sweepDirX = (float) Math.cos(sweepAngle);
        float sweepDirY = (float) Math.sin(sweepAngle);
        float sweepPos = (tickCounter * 0.35f) % (FW + FH + 20) - 10;

        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float proj = lx * sweepDirX + ly * sweepDirY;
                float dist = Math.abs(proj - sweepPos);
                if (dist < 6) {
                    float intensity = (float)(Math.cos(dist / 6 * Math.PI) + 1) * 0.1f;
                    faceBlend(img, lx, ly, 0xFFD0A0FF, intensity);
                }
            }
        }

        // Crystal glints (sharp flashes)
        for (int i = 0; i < P_COUNT; i++) {
            float flash = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.055 + pPhase[i])), 7);
            if (flash > 0.05f) {
                int lx = (int) pX[i], ly = (int) pY[i];
                float sz = pSize[i] * (1 + flash * 2);
                faceBlend(img, lx, ly, 0xFFCCA0FF, flash * 0.9f);
                for (int f = 1; f <= (int) sz; f++) {
                    float fade = 1.0f - f / (sz + 1);
                    faceBlend(img, lx + f, ly, 0xFFAA80DD, flash * fade * 0.4f);
                    faceBlend(img, lx - f, ly, 0xFFAA80DD, flash * fade * 0.4f);
                    faceBlend(img, lx, ly + f, 0xFFAA80DD, flash * fade * 0.4f);
                    faceBlend(img, lx, ly - f, 0xFFAA80DD, flash * fade * 0.4f);
                }
            }
        }

        // Purple ambient pulse
        float pulse = (float)(Math.sin(tickCounter * 0.02) * 0.03);
        if (Math.abs(pulse) > 0.005f) {
            for (int ly = 0; ly < FH; ly += 2) {
                for (int lx = 0; lx < FW; lx += 2) {
                    faceBlend(img, lx, ly, 0xFFBB80EE, Math.abs(pulse));
                }
            }
        }
    }

    // ========== INFERNO - hellfire + skull glow + ember storm ==========
    private static void animateInferno(NativeImage img) {
        float timeOffset = tickCounter * 0.07f;

        // Scrolling hellfire (upward from bottom)
        for (int ly = 0; ly < FH; ly++) {
            float yFactor = (float) ly / FH;
            for (int lx = 0; lx < FW; lx++) {
                float n1 = noise2D(lx * 0.05f, ly * 0.04f - timeOffset);
                float n2 = noise2D(lx * 0.1f + 30, ly * 0.07f - timeOffset * 1.4f);
                float fire = (n1 * 0.5f + n2 * 0.5f) * yFactor * yFactor;
                if (fire > 0.12f) {
                    float intensity = (fire - 0.12f) * 0.5f;
                    int fr, fg;
                    if (intensity > 0.2f) { fr = 255; fg = 100; }
                    else if (intensity > 0.1f) { fr = 220; fg = 40; }
                    else { fr = 180; fg = 15; }
                    faceBlend(img, lx, ly, argb(255, fr, fg, 0), Math.min(0.3f, intensity));
                }
            }
        }

        // Pulsing skull eye glow
        float eyeGlow = (float)(Math.sin(tickCounter * 0.08) * 0.5 + 0.5);
        int[][] skullEyes = {{22 - 3, 30 - 3}, {22 + 3, 30 - 3}, {58 - 3, 55 - 2}, {58 + 3, 55 - 2},
            {35 - 4, 85 - 3}, {35 + 4, 85 - 3}, {68 - 2, 105 - 2}, {68 + 2, 105 - 2}};
        for (int[] eye : skullEyes) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d <= 2) {
                        faceBlend(img, eye[0] + dx, eye[1] + dy, 0xFFFF5500, (1 - d / 2) * eyeGlow * 0.5f);
                    }
                }
            }
        }

        // Rising ember particles
        if (!particlesInited) {
            Random r = new Random(666);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextFloat() * FW;
                pY[i] = FH - r.nextFloat() * 10;
                pDY[i] = -(0.2f + r.nextFloat() * 0.3f);
                pDX[i] = (r.nextFloat() - 0.5f) * 0.15f;
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 1 + r.nextFloat() * 2;
                pLife[i] = 1.0f;
            }
            particlesInited = true;
        }

        for (int i = 0; i < P_COUNT; i++) {
            pY[i] += pDY[i];
            pX[i] += pDX[i] + (float) Math.sin(tickCounter * 0.07 + pPhase[i]) * 0.15f;
            pLife[i] -= 0.004f;
            if (pY[i] < -3 || pLife[i] <= 0) {
                pY[i] = FH - new Random().nextFloat() * 5;
                pX[i] = new Random().nextFloat() * FW;
                pLife[i] = 1.0f;
            }
            int lx = ((int) pX[i]) % FW;
            if (lx < 0) lx += FW;
            int ly = (int) pY[i];
            float bright = (pY[i] / FH) * pLife[i];
            faceBlend(img, lx, ly, argb(255, 255, (int)(80 * bright), 0), bright * 0.7f);
        }

        // Intense flicker at bottom
        float flicker = (float)(Math.sin(tickCounter * 0.25) * 0.5 + 0.5) * 0.12f;
        for (int ly = FH - 15; ly < FH; ly++) {
            float s = (float)(ly - (FH - 15)) / 15.0f;
            for (int lx = 0; lx < FW; lx++) {
                faceBlend(img, lx, ly, 0xFFFF4400, s * flicker);
            }
        }
    }

    // ========== DRIFT - slowly shifting pastel gradient ==========
    private static void animateDrift(NativeImage img) {
        float timeShift = tickCounter * 0.008f;

        // Slowly shift the entire gradient phase
        for (int ly = 0; ly < FH; ly++) {
            float t = (float) ly / (FH - 1);
            for (int lx = 0; lx < FW; lx++) {
                float xt = (float) lx / FW;
                float phase = (xt * 0.5f + t * 0.5f + timeShift) * 3;
                float n = noise2D(lx * 0.03f + timeShift * 2, ly * 0.03f) * 0.08f;
                phase += n;

                int r = Math.max(0, Math.min(255, (int)(200 + 55 * Math.sin(phase))));
                int g = Math.max(0, Math.min(255, (int)(180 + 60 * Math.sin(phase + 2.1))));
                int b = Math.max(0, Math.min(255, (int)(220 + 35 * Math.sin(phase + 4.2))));

                // Blend with base rather than overwrite fully
                faceBlend(img, lx, ly, argb(255, r, g, b), 0.3f);
            }
        }

        // Vaporwave sun pulse at bottom
        float sunPulse = (float)(Math.sin(tickCounter * 0.03) * 0.5 + 0.5);
        int sunCy = FH - 20;
        for (int stripe = 0; stripe < 5; stripe++) {
            int sy = sunCy + stripe * 4;
            float stripeAlpha = 0.1f + sunPulse * 0.08f;
            for (int lx = 0; lx < FW; lx++) {
                float d = Math.abs(lx - FW / 2f) / (FW / 2f);
                if (d < 0.8f) faceBlend(img, lx, sy, 0xFFFF78C8, stripeAlpha * (1 - d));
            }
        }

        // Soft sparkle drift
        for (int i = 0; i < 8; i++) {
            float sx = (pseudoRand(i * 7, (int)(tickCounter * 0.03f)) * FW);
            float sy = (pseudoRand(i * 13, (int)(tickCounter * 0.02f)) * FH);
            float bright = (float)(Math.sin(tickCounter * 0.06 + i * 2) * 0.5 + 0.5);
            if (bright > 0.5f) {
                faceBlend(img, (int) sx, (int) sy, 0xFFFFFFFF, (bright - 0.5f) * 0.6f);
            }
        }

        // Gentle color breathing
        float breath = (float)(Math.sin(tickCounter * 0.012) * 0.025);
        if (Math.abs(breath) > 0.005f) {
            int overlay = breath > 0 ? 0xFFFFDDEE : 0xFFDDEEFF;
            for (int ly = 0; ly < FH; ly += 2) {
                for (int lx = 0; lx < FW; lx += 2) {
                    faceBlend(img, lx, ly, overlay, Math.abs(breath));
                }
            }
        }
    }

    // ========== OBSIDIAN - pulsing purple cracks + dark energy ==========
    private static void animateObsidian(NativeImage img) {
        if (!particlesInited) {
            Random r = new Random(550);
            for (int i = 0; i < P_COUNT; i++) {
                pX[i] = r.nextInt(FW);
                pY[i] = r.nextInt(FH);
                pPhase[i] = r.nextFloat() * 6.28f;
                pSize[i] = 1 + r.nextFloat() * 2;
            }
            particlesInited = true;
        }

        // Pulsing crack glow
        float crackPulse = (float)(Math.sin(tickCounter * 0.04) * 0.5 + 0.5);
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                // Re-check voronoi cracks and pulse them
                float v = noise2D(lx * 0.15f, ly * 0.12f);
                if (v < 0.15f) {
                    float crack = (0.15f - v) / 0.15f;
                    float alpha = crack * crackPulse * 0.25f;
                    faceBlend(img, lx, ly, 0xFF9030E0, alpha);
                }
            }
        }

        // Dark energy wisps
        float wispPhase = tickCounter * 0.03f;
        for (int wisp = 0; wisp < 4; wisp++) {
            float angle = wispPhase + wisp * 1.57f;
            int wcx = FW / 2 + (int)(Math.cos(angle) * 25);
            int wcy = FH / 2 + (int)(Math.sin(angle) * 40);
            for (int dy = -8; dy <= 8; dy++) {
                for (int dx = -5; dx <= 5; dx++) {
                    float d = (float) Math.sqrt(dx * dx * 1.5f + dy * dy * 0.5f);
                    if (d < 8) {
                        float alpha = (1 - d / 8) * 0.15f;
                        faceBlend(img, wcx + dx, wcy + dy, 0xFF6020A0, alpha);
                    }
                }
            }
        }

        // Rare purple glint flashes
        for (int i = 0; i < P_COUNT; i++) {
            float flash = (float) Math.pow(Math.max(0, Math.sin(tickCounter * 0.045 + pPhase[i])), 10);
            if (flash > 0.1f) {
                int lx = (int) pX[i], ly = (int) pY[i];
                faceBlend(img, lx, ly, 0xFFA060E0, flash * 0.8f);
                faceBlend(img, lx + 1, ly, 0xFF8040C0, flash * 0.3f);
                faceBlend(img, lx - 1, ly, 0xFF8040C0, flash * 0.3f);
                faceBlend(img, lx, ly + 1, 0xFF8040C0, flash * 0.3f);
                faceBlend(img, lx, ly - 1, 0xFF8040C0, flash * 0.3f);
            }
        }

        // Slow dark shimmer sweep
        float shimmer = (tickCounter * 0.3f) % (FW + FH + 12) - 6;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dist = Math.abs((lx + ly) - shimmer);
                if (dist < 5) {
                    float intensity = (float)(Math.cos(dist / 5 * Math.PI) + 1) * 0.06f;
                    faceBlend(img, lx, ly, 0xFF7030B0, intensity);
                }
            }
        }
    }

    // ========== BLACK HOLE ==========
    private static void animateBlackHole(NativeImage img) {
        float cx = FW / 2.0f, cy = FH * 0.42f;
        float time = tickCounter * 0.02f;

        // Rotating accretion disk overlay
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dx = lx - cx, dy = (ly - cy) * 2.8f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);

                // Only affect the disk region
                if (dist > 12 && dist < 38) {
                    float ringT = (dist - 12) / 26.0f;
                    float diskIntensity = (float)(Math.sin(ringT * Math.PI) * 0.5f);

                    // Rotating hot spots — simulate matter clumps orbiting
                    float rotAngle = angle - time * (1.5f + (1.0f - ringT) * 2.0f); // inner rotates faster
                    float hotspot1 = (float)Math.pow(Math.max(0, Math.cos(rotAngle * 1.5f)), 4) * 0.5f;
                    float hotspot2 = (float)Math.pow(Math.max(0, Math.cos(rotAngle * 2.3f + 1.8f)), 6) * 0.3f;
                    float glow = (hotspot1 + hotspot2) * diskIntensity;

                    if (glow > 0.02f) {
                        // White-hot inner, orange outer
                        int r, g, b;
                        if (ringT < 0.35f) {
                            r = 255; g = (int)(240 - ringT * 200); b = (int)(220 - ringT * 500);
                        } else {
                            r = 255; g = (int)(160 - (ringT - 0.35f) * 250); b = (int)(40 - (ringT - 0.35f) * 60);
                        }
                        faceBlend(img, lx, ly, (0xFF << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b), glow);
                    }
                }
            }
        }

        // Pulsing event horizon edge
        float horizonPulse = (float)(Math.sin(time * 3) * 0.5f + 0.5f) * 0.15f;
        for (int ly = 0; ly < FH; ly++) {
            for (int lx = 0; lx < FW; lx++) {
                float dx = lx - cx, dy = (ly - cy) * 2.8f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > 9 && dist < 14) {
                    float edgeFade = 1.0f - Math.abs(dist - 11.5f) / 2.5f;
                    faceBlend(img, lx, ly, 0xFFFFA040, edgeFade * horizonPulse);
                }
            }
        }

        // Stars being sucked in — spiral particle paths
        if (!particlesInited) {
            Random rand = new Random(777);
            for (int i = 0; i < P_COUNT; i++) {
                pPhase[i] = rand.nextFloat() * (float)(Math.PI * 2);
                pSize[i] = 18 + rand.nextFloat() * 25;
                pLife[i] = rand.nextFloat();
                pRot[i] = 0.02f + rand.nextFloat() * 0.04f;
            }
            particlesInited = true;
        }

        for (int i = 0; i < P_COUNT; i++) {
            // Spiral inward
            float angle = pPhase[i] + tickCounter * pRot[i];
            float radius = pSize[i] * (1.0f - pLife[i]);
            float px = cx + (float)Math.cos(angle) * radius;
            float py = cy + (float)Math.sin(angle) * radius / 2.8f; // compressed for the ellipse

            pLife[i] += 0.004f + 0.002f * (1.0f - radius / 40.0f); // accelerate as they get closer
            if (pLife[i] > 1.0f || radius < 5) {
                // Respawn at edge
                pLife[i] = 0;
                pPhase[i] += 2.5f;
                pSize[i] = 18 + (float)(Math.abs(Math.sin(i * 3.7f)) * 25);
            }

            float starAlpha = Math.min(1.0f, (1.0f - pLife[i]) * 3) * 0.7f;
            // Stretch color from blue-white to orange as it falls in
            int sr = (int)(200 + pLife[i] * 55);
            int sg = (int)(220 - pLife[i] * 120);
            int sb = (int)(255 - pLife[i] * 200);
            faceBlend(img, (int)px, (int)py, (0xFF << 24) | (clamp(sr) << 16) | (clamp(sg) << 8) | clamp(sb), starAlpha);
            faceBlend(img, (int)px + 1, (int)py, (0xFF << 24) | (clamp(sr) << 16) | (clamp(sg) << 8) | clamp(sb), starAlpha * 0.4f);
        }

        // Pulsing jets
        float jetPulse = (float)(Math.sin(time * 2.5f) * 0.3f + 0.7f);
        for (int ly = 0; ly < FH; ly++) {
            float distFromCenter = Math.abs(ly - cy);
            if (distFromCenter > 14) {
                float jetIntensity = 0.08f * jetPulse * (1.0f - Math.min(1.0f, distFromCenter / (FH * 0.5f)));
                float wobble = (float)Math.sin(ly * 0.2f + time * 4) * 1.5f;
                for (int dx = -2; dx <= 2; dx++) {
                    float falloff = 1.0f - Math.abs(dx - wobble) / 3.5f;
                    if (falloff > 0) {
                        faceBlend(img, (int)cx + dx, ly, 0xFFB0A0FF, jetIntensity * falloff);
                    }
                }
            }
        }
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
