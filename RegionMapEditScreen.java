package dev.krypton.regionmap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegionMapEditScreen extends Screen {
    private final RegionMapConfig cfg;
    private boolean dragging, resizing;
    private double offX, offY;

    public RegionMapEditScreen(RegionMapConfig cfg) {
        super(Text.literal("Region Map"));
        this.cfg = cfg;
    }

    private static class Slider extends SliderWidget {
        private final String label; private final double min, max; private final Consumer<Double> set; private final boolean pct;
        Slider(int x, int y, int w, String label, double min, double max, double val, boolean pct, Consumer<Double> set) {
            super(x, y, w, 16, Text.empty(), (val - min) / (max - min));
            this.label = label; this.min = min; this.max = max; this.set = set; this.pct = pct;
            updateMessage();
        }
        @Override protected void updateMessage() {
            if (label == null) return;
            double v = min + value * (max - min);
            setMessage(Text.literal(label + ": " + (pct ? (int) Math.round(v * 100) + "%" : String.valueOf((int) Math.round(v)))));
        }
        @Override protected void applyValue() { set.accept(min + value * (max - min)); }
    }

    private void toggle(int x, int y, int w, String name, Supplier<Boolean> get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + (get.get() ? "ON" : "OFF")), b -> {
            set.accept(!get.get());
            b.setMessage(Text.literal(name + ": " + (get.get() ? "ON" : "OFF")));
        }).dimensions(x, y, w, 16).build());
    }

    @Override
    protected void init() {
        int w = 130, x = width - w - 8, y = 8, g = 18;
        toggle(x, y, w, "Enabled", () -> cfg.enabled, v -> cfg.enabled = v); y += g;
        addDrawableChild(new Slider(x, y, w, "Size", 80, 360, cfg.size, false, v -> cfg.size = v.intValue())); y += g;
        addDrawableChild(new Slider(x, y, w, "Opacity", 0.1, 1.0, cfg.opacity, true, v -> cfg.opacity = v.floatValue())); y += g;
        addDrawableChild(new Slider(x, y, w, "Corners", 2, 24, cfg.cornerRadius, false, v -> cfg.cornerRadius = v.intValue())); y += g;
        addDrawableChild(new Slider(x, y, w, "Borders", 1, 6, cfg.borderThickness, false, v -> cfg.borderThickness = v.intValue())); y += g;
        toggle(x, y, w, "Labels", () -> cfg.showLabels, v -> cfg.showLabels = v); y += g;
        toggle(x, y, w, "Grid", () -> cfg.showGrid, v -> cfg.showGrid = v); y += g;
        toggle(x, y, w, "Coords", () -> cfg.showCoords, v -> cfg.showCoords = v); y += g;
        addDrawableChild(ButtonWidget.builder(Text.literal("Colours: " + RegionMapConfig.MODES[cfg.colorMode]), b -> {
            cfg.colorMode = (cfg.colorMode + 1) % RegionMapConfig.MODES.length;
            b.setMessage(Text.literal("Colours: " + RegionMapConfig.MODES[cfg.colorMode]));
        }).dimensions(x, y, w, 16).build()); y += g;
        addDrawableChild(ButtonWidget.builder(Text.literal("Accent: " + RegionMapConfig.ACCENT_NAMES[cfg.accentIdx]), b -> {
            cfg.accentIdx = (cfg.accentIdx + 1) % RegionMapConfig.ACCENTS.length;
            b.setMessage(Text.literal("Accent: " + RegionMapConfig.ACCENT_NAMES[cfg.accentIdx]));
        }).dimensions(x, y, w, 16).build()); y += g;
        addDrawableChild(ButtonWidget.builder(Text.literal("Glass: " + RegionMapConfig.TINT_NAMES[cfg.tintIdx]), b -> {
            cfg.tintIdx = (cfg.tintIdx + 1) % RegionMapConfig.TINTS.length;
            b.setMessage(Text.literal("Glass: " + RegionMapConfig.TINT_NAMES[cfg.tintIdx]));
        }).dimensions(x, y, w, 16).build()); y += g;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> { cfg.reset(); clearAndInit(); })
                .dimensions(x, y, w / 2 - 1, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x + w / 2 + 1, y, w / 2 - 1, 16).build());
    }

    // Vanilla renderBackground() applies the menu blur. Overriding it means this screen
    // never requests a blur pass at all, so it can't clash with another blur.
    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        renderBackground(c, mouseX, mouseY, delta);
        RegionMapRenderer.render(c, cfg, true);
        c.drawText(textRenderer, "Drag to move \u00b7 drag corner or scroll to resize", 10, height - 14, 0xFFFFFFFF, true);
        super.render(c, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        int w = RegionMapRenderer.panelW(cfg), h = RegionMapRenderer.panelH(cfg);
        if (button == 0 && mx >= cfg.x + w - 12 && mx <= cfg.x + w && my >= cfg.y + h - 12 && my <= cfg.y + h) {
            resizing = true; return true;
        }
        if (button == 0 && mx >= cfg.x && mx <= cfg.x + w && my >= cfg.y && my <= cfg.y + h) {
            dragging = true; offX = mx - cfg.x; offY = my - cfg.y; return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (resizing) {
            cfg.size = Math.max(80, Math.min(360, (int) (mx - cfg.x) - RegionMapRenderer.PAD * 2 + 4));
            return true;
        }
        if (dragging) {
            cfg.x = (int) Math.max(0, Math.min(width - RegionMapRenderer.panelW(cfg), mx - offX));
            cfg.y = (int) Math.max(0, Math.min(height - RegionMapRenderer.panelH(cfg), my - offY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        boolean wasResizing = resizing;
        dragging = false; resizing = false;
        if (wasResizing) clearAndInit();
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int w = RegionMapRenderer.panelW(cfg), h = RegionMapRenderer.panelH(cfg);
        if (mx >= cfg.x && mx <= cfg.x + w && my >= cfg.y && my <= cfg.y + h) {
            cfg.size = Math.max(80, Math.min(360, cfg.size + (int) (vAmount * 10)));
            clearAndInit();
            return true;
        }
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { cfg.save(); }
}
