package com.regionmap.krmap;

import net.minecraft.client.gui.DrawContext;

/**
 * "Liquid glass" panel drawn ONLY with plain fills/gradients.
 * No shader blur and no post-effect, so it can never collide with the
 * game's one-blur-per-frame limit (menu background blur etc).
 */
final class GlassPainter {
    static int argb(int rgb, int a) { return (a << 24) | (rgb & 0xFFFFFF); }

    private static int inset(int r, int i) {
        if (r <= 0) return 0;
        double dy = r - i - 0.5;
        return (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
    }

    static void roundedFill(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            c.fill(x + in, y + i, x + w - in, y + i + 1, col);
            c.fill(x + in, y + h - i - 1, x + w - in, y + h - i, col);
        }
        c.fill(x, y + r, x + w, y + h - r, col);
    }

    static void roundedOutline(DrawContext c, int x, int y, int w, int h, int r, int topCol, int botCol) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        int mid = y + h / 2;
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            int prev = i == 0 ? r : inset(r, i - 1);
            int len = Math.max(1, prev - in);
            int ct = (y + i < mid) ? topCol : botCol;
            c.fill(x + in, y + i, x + in + len, y + i + 1, ct);
            c.fill(x + w - in - len, y + i, x + w - in, y + i + 1, ct);
            int yb = y + h - 1 - i;
            int cb = (yb < mid) ? topCol : botCol;
            c.fill(x + in, yb, x + in + len, yb + 1, cb);
            c.fill(x + w - in - len, yb, x + w - in, yb + 1, cb);
        }
        c.fill(x + r, y, x + w - r, y + 1, topCol);
        c.fill(x + r, y + h - 1, x + w - r, y + h, botCol);
        c.fillGradient(x, y + r, x + 1, y + h - r, topCol, botCol);
        c.fillGradient(x + w - 1, y + r, x + w, y + h - r, topCol, botCol);
    }

    static void panel(DrawContext c, int x, int y, int w, int h, int r, int tint, float opacity) {
        // soft layered drop shadow
        for (int k = 4; k >= 1; k--) roundedFill(c, x - k, y - k + 3, w + 2 * k, h + 2 * k, r + k, 0x0C000000);
        // glass body
        roundedFill(c, x, y, w, h, r, argb(tint, (int) (0x38 + 0x28 * opacity)));
        // top refraction highlight + bottom inner shadow
        c.fillGradient(x + r, y + 1, x + w - r, y + h / 2, argb(tint, 0x55), argb(tint, 0x00));
        c.fillGradient(x + r, y + h / 2, x + w - r, y + h - 1, 0x00000000, 0x38000000);
        // rims
        roundedOutline(c, x, y, w, h, r, 0xD0FFFFFF, 0x45FFFFFF);
        roundedOutline(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), 0x30FFFFFF, 0x08FFFFFF);
        // specular glint
        c.fill(x + r + 3, y + 1, x + r + 3 + Math.max(6, w / 5), y + 2, 0xFFFFFFFF);
        c.fill(x + 2, y + r + 2, x + 3, y + r + 2 + Math.max(6, h / 8), 0x90FFFFFF);
    }

    private GlassPainter() {}
}
