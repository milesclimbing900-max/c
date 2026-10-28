package com.regionmap.krmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class RegionMapConfig {
    public static final int[] ACCENTS = {0x5AD1FF, 0xFF5A8A, 0xA78BFA, 0x4ADE80, 0xFFFFFF};
    public static final String[] ACCENT_NAMES = {"Cyan", "Pink", "Violet", "Green", "White"};
    public static final int[] TINTS = {0xFFFFFF, 0xBFDFFF, 0xD8C8FF, 0x20242A};
    public static final String[] TINT_NAMES = {"Frost", "Ice", "Lilac", "Smoke"};
    public static final String[] MODES = {"Mono", "Ice", "Heat"};

    public boolean enabled = true;
    public int x = 10, y = 10;
    public int size = 150;          // side of the square map in GUI pixels
    public float opacity = 0.85f;   // map cell opacity
    public int cornerRadius = 12;
    public int borderThickness = 2;
    public int colorMode = 0;
    public int accentIdx = 0;
    public int tintIdx = 1;
    public boolean showLabels = true, showGrid = false, showCoords = true;
    public double[] density = RegionData.defaultDensity();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("krypton_regionmap.json"); }

    public static RegionMapConfig load() {
        try {
            if (Files.exists(path())) {
                RegionMapConfig c = GSON.fromJson(Files.readString(path()), RegionMapConfig.class);
                if (c != null) return c.sanitise();
            }
        } catch (Exception ignored) {}
        RegionMapConfig c = new RegionMapConfig();
        c.save();
        return c;
    }

    public void save() {
        try { Files.writeString(path(), GSON.toJson(this)); } catch (Exception ignored) {}
    }

    public void reset() {
        RegionMapConfig d = new RegionMapConfig();
        enabled = d.enabled; x = d.x; y = d.y; size = d.size; opacity = d.opacity;
        cornerRadius = d.cornerRadius; borderThickness = d.borderThickness; colorMode = d.colorMode;
        accentIdx = d.accentIdx; tintIdx = d.tintIdx; showLabels = d.showLabels;
        showGrid = d.showGrid; showCoords = d.showCoords; density = d.density;
    }

    private RegionMapConfig sanitise() {
        if (density == null || density.length != RegionData.N * RegionData.N) density = RegionData.defaultDensity();
        size = Math.max(80, Math.min(360, size));
        opacity = Math.max(0.1f, Math.min(1f, opacity));
        cornerRadius = Math.max(0, Math.min(24, cornerRadius));
        borderThickness = Math.max(1, Math.min(6, borderThickness));
        colorMode = Math.floorMod(colorMode, MODES.length);
        accentIdx = Math.floorMod(accentIdx, ACCENTS.length);
        tintIdx = Math.floorMod(tintIdx, TINTS.length);
        return this;
    }
}
