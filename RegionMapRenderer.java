package dev.krypton.regionmap;

import dev.krypton.regionmap.RegionData.Region;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class RegionMapRenderer {
    public static final int PAD = 6;
    public static final int FOOT = 12;

    private static final int[][] STOPS_MONO = {{0, 0, 0}, {255, 255, 255}};
    private static final int[][] STOPS_ICE  = {{0, 0, 0}, {20, 60, 160}, {90, 170, 255}, {255, 255, 255}};
    private static final int[][] STOPS_HEAT = {{0, 0, 0}, {150, 20, 30}, {255, 130, 60}, {255, 255, 255}};

    // region label anchors (centroid of each region's cells)
    private static final float[][] CENTROID = new float[Region.values().length][3];
    static {
        for (int r = 0; r < RegionData.N; r++)
            for (int c = 0; c < RegionData.N; c++) {
                float[] a = CENTROID[RegionData.regionAt(c, r).ordinal()];
                a[0] += c + 0.5f; a[1] += r + 0.5f; a[2] += 1;
            }
    }

    public static int panelW(RegionMapConfig cfg) { return cfg.size + PAD * 2; }
    public static int panelH(RegionMapConfig cfg) { return cfg.size + PAD * 2 + (cfg.showCoords ? FOOT : 0); }

    private static int ramp(double n, int mode, int alpha) {
        int[][] s = mode == 1 ? STOPS_ICE : mode == 2 ? STOPS_HEAT : STOPS_MONO;
        n = Math.max(0, Math.min(1, n));
        double f = n * (s.length - 1);
        int i = Math.min((int) f, s.length - 2);
        double t = f - i;
        int r = (int) (s[i][0] + (s[i + 1][0] - s[i][0]) * t);
        int g = (int) (s[i][1] + (s[i + 1][1] - s[i][1]) * t);
        int b = (int) (s[i][2] + (s[i + 1][2] - s[i][2]) * t);
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static void render(DrawContext c, RegionMapConfig cfg, boolean editing) {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean has = mc.player != null;
        double wx = has ? mc.player.getX() : 0, wz = has ? mc.player.getZ() : 0;
        float yaw = has ? mc.player.getYaw() : 0;

        final int M = cfg.size, W = panelW(cfg), H = panelH(cfg), x = cfg.x, y = cfg.y;
        GlassPainter.panel(c, x, y, W, H, cfg.cornerRadius, RegionMapConfig.TINTS[cfg.tintIdx], cfg.opacity);

        int mx = x + PAD, my = y + PAD, n = RegionData.N;
        int[] ex = new int[n + 1], ey = new int[n + 1];
        for (int i = 0; i <= n; i++) { ex[i] = mx + Math.round(i * M / (float) n); ey[i] = my + Math.round(i * M / (float) n); }

        double max = 0.0001;
        for (double d : cfg.density) max = Math.max(max, d);
        int alpha = (int) (255 * cfg.opacity);

        // 1) density cells: black = no RTPs ... white = most RTPs
        for (int r = 0; r < n; r++)
            for (int col = 0; col < n; col++)
                c.fill(ex[col], ey[r], ex[col + 1], ey[r + 1], ramp(cfg.density[r * n + col] / max, cfg.colorMode, alpha));

        // 2) optional cell grid
        if (cfg.showGrid) {
            for (int i = 1; i < n; i++) {
                c.fill(ex[i], my, ex[i] + 1, my + M, 0x40000000);
                c.fill(mx, ey[i], mx + M, ey[i] + 1, 0x40000000);
            }
        }

        // 3) strong region borders (dark outline + each side coloured by its own region)
        int t = cfg.borderThickness, tl = t / 2, tr = t - tl;
        for (int pass = 0; pass < 2; pass++) {
            for (int r = 0; r < n; r++)
                for (int col = 0; col < n; col++) {
                    Region a = RegionData.regionAt(col, r);
                    if (col + 1 < n) {
                        Region b = RegionData.regionAt(col + 1, r);
                        if (a != b) {
                            int bx = ex[col + 1];
                            if (pass == 0) c.fill(bx - tl - 1, ey[r], bx + tr + 1, ey[r + 1], 0xD0000000);
                            else { c.fill(bx - tl, ey[r], bx, ey[r + 1], a.color); c.fill(bx, ey[r], bx + tr, ey[r + 1], b.color); }
                        }
                    }
                    if (r + 1 < n) {
                        Region b = RegionData.regionAt(col, r + 1);
                        if (a != b) {
                            int by = ey[r + 1];
                            if (pass == 0) c.fill(ex[col], by - tl - 1, ex[col + 1], by + tr + 1, 0xD0000000);
                            else { c.fill(ex[col], by - tl, ex[col + 1], by, a.color); c.fill(ex[col], by, ex[col + 1], by + tr, b.color); }
                        }
                    }
                }
        }
        // map frame
        c.fill(mx - 1, my - 1, mx + M + 1, my, 0xB0FFFFFF);
        c.fill(mx - 1, my + M, mx + M + 1, my + M + 1, 0xB0FFFFFF);
        c.fill(mx - 1, my, mx, my + M, 0xB0FFFFFF);
        c.fill(mx + M, my, mx + M + 1, my + M, 0xB0FFFFFF);

        // 4) region labels
        if (cfg.showLabels && M >= 110) {
            float s = M >= 200 ? 0.8f : 0.6f;
            for (Region rg : Region.values()) {
                float[] a = CENTROID[rg.ordinal()];
                float cx = mx + a[0] / a[2] * M / n, cy = my + a[1] / a[2] * M / n;
                int tw = mc.textRenderer.getWidth(rg.label);
                c.getMatrices().push();
                c.getMatrices().translate(cx, cy, 0);
                c.getMatrices().scale(s, s, 1f);
                c.fill(-tw / 2 - 2, -1, tw / 2 + 2, 9, 0x90000000);
                c.drawText(mc.textRenderer, rg.label, -tw / 2, 0, 0xFFFFFFFF, false);
                c.getMatrices().pop();
            }
        }

        // 5) current cell + player
        int pc = RegionData.cellOf(wx), pr = RegionData.cellOf(wz);
        c.fill(ex[pc], ey[pr], ex[pc + 1], ey[pr + 1], 0x30FFFFFF);
        c.fill(ex[pc], ey[pr], ex[pc + 1], ey[pr] + 1, 0xC0FFFFFF);
        c.fill(ex[pc], ey[pr + 1] - 1, ex[pc + 1], ey[pr + 1], 0xC0FFFFFF);
        c.fill(ex[pc], ey[pr], ex[pc] + 1, ey[pr + 1], 0xC0FFFFFF);
        c.fill(ex[pc + 1] - 1, ey[pr], ex[pc + 1], ey[pr + 1], 0xC0FFFFFF);

        if (has) {
            double fx = Math.max(0, Math.min(1, (wx + RegionData.HALF) / (RegionData.HALF * 2.0)));
            double fz = Math.max(0, Math.min(1, (wz + RegionData.HALF) / (RegionData.HALF * 2.0)));
            int px = mx + (int) (fx * M), pz = my + (int) (fz * M);
            int accent = 0xFF000000 | RegionMapConfig.ACCENTS[cfg.accentIdx];

            float pulse = (System.currentTimeMillis() % 1500) / 1500f;
            int rr = 3 + (int) (pulse * 7), pa = (int) ((1f - pulse) * 0x90);
            int ring = (pa << 24) | (accent & 0xFFFFFF);
            c.fill(px - rr, pz - rr, px + rr + 1, pz - rr + 1, ring);
            c.fill(px - rr, pz + rr, px + rr + 1, pz + rr + 1, ring);
            c.fill(px - rr, pz - rr, px - rr + 1, pz + rr + 1, ring);
            c.fill(px + rr, pz - rr, px + rr + 1, pz + rr + 1, ring);

            double rad = Math.toRadians(yaw), dx = -Math.sin(rad), dz = Math.cos(rad);
            for (int k = 3; k <= 8; k++) {
                int ax = px + (int) Math.round(dx * k), az = pz + (int) Math.round(dz * k);
                c.fill(ax - 1, az - 1, ax + 1, az + 1, 0xFFFFFFFF);
            }
            c.fill(px - 3, pz - 3, px + 4, pz + 4, 0xFF000000);
            c.fill(px - 2, pz - 2, px + 3, pz + 3, accent);
        }

        // 6) footer
        if (cfg.showCoords) {
            String txt = (has ? String.format("X %,d  Z %,d", (int) wx, (int) wz) : "X -  Z -") + "  \u00b7  " + RegionData.regionAt(pc, pr).label;
            float s = Math.min(1f, (M - 4f) / Math.max(1, mc.textRenderer.getWidth(txt)));
            s = Math.min(s, 0.85f);
            c.getMatrices().push();
            c.getMatrices().translate(mx + 2, my + M + 3, 0);
            c.getMatrices().scale(s, s, 1f);
            c.drawText(mc.textRenderer, txt, 0, 0, 0xFFFFFFFF, true);
            c.getMatrices().pop();
        }

        // 7) edit-mode overlay
        if (editing) {
            int a = 0xFF000000 | RegionMapConfig.ACCENTS[cfg.accentIdx];
            c.fill(x - 2, y - 2, x + W + 2, y - 1, a);
            c.fill(x - 2, y + H + 1, x + W + 2, y + H + 2, a);
            c.fill(x - 2, y - 2, x - 1, y + H + 2, a);
            c.fill(x + W + 1, y - 2, x + W + 2, y + H + 2, a);
            c.fill(x + W - 9, y + H - 9, x + W - 1, y + H - 1, a);
        }
    }

    private RegionMapRenderer() {}
}
