package dev.krypton.regionmap;

/** 9x9 grid of 50k-block cells covering -225k..+225k on X and Z. */
public final class RegionData {
    public static final int N = 9;
    public static final int CELL = 50_000;
    public static final int HALF = 225_000;

    public enum Region {
        OCEANIA('O', "Oceania", 0xFFD9625E),
        NA_WEST('W', "NA West", 0xFFDDA52F),
        NA_EAST('E', "NA East", 0xFFD95B21),
        ASIA('A', "Asia", 0xFF7545D8),
        EU_WEST('G', "EU West", 0xFF3FA84E),
        EU_CENTRAL('C', "EU Central", 0xFF3378D8);

        public final char key;
        public final String label;
        public final int color;

        Region(char key, String label, int color) { this.key = key; this.label = label; this.color = color; }

        static Region of(char k) {
            for (Region r : values()) if (r.key == k) return r;
            return EU_CENTRAL;
        }
    }

    // Row = Z (top is -225k), column = X (left is -225k). Matches cells 1..81 of the border map.
    private static final String[] LAYOUT = {
        "OWWWEEEEE",
        "OWWWEEEEE",
        "OWWWEEEEE",
        "OOOWEEEEE",
        "AAAAEEEEE",
        "AGGCCCCCE",
        "AGGCCCCCE",
        "CGCCCCCCC",
        "CGGGGGGCC"
    };

    // Relative RTP density per cell (0..1), estimated from the density screenshot.
    // Editable in config/krypton_regionmap.json ("density", row-major, 81 values).
    private static final double[] DENSITY = {
        .50,.55,.55,.50,.15,.15,.10,.20,.15,
        .50,.60,.65,.60,.85,.85,.60,.85,.15,
        .60,.50,.60,.50,.20,.08,.20,.20,.20,
        .60,.45,.45,.35,.10,.08,.20,.20,.20,
        .30,.40,.70,.65,.02,.20,.80,.10,.15,
        .40,.40,.40,.30,.30,.45,.35,.25,.10,
        .30,.40,.40,.25,.25,.25,.30,.20,.15,
        .60,.30,.50,.40,.55,.45,.60,.35,.30,
        .20,.40,.70,.60,.45,.55,.45,.15,.20
    };

    public static double[] defaultDensity() { return DENSITY.clone(); }

    public static Region regionAt(int col, int row) { return Region.of(LAYOUT[row].charAt(col)); }

    public static int cellOf(double world) {
        int c = (int) Math.floor((world + HALF) / (double) CELL);
        return Math.max(0, Math.min(N - 1, c));
    }

    private RegionData() {}
}
