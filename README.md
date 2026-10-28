# Krypton Region Map

Liquid-glass HUD map of RTP density across the 450k x 450k world (9x9 grid of 50k cells).

- Black = no RTPs, white = most RTPs (switch palette: Mono / Ice / Heat)
- Strong, region-coloured borders between EU Central / EU West / NA East / NA West / Asia / Oceania
- Pulsing player marker with facing arrow, current cell highlighted, live X/Z + region
- Press **M** to edit: drag to move, drag the corner or scroll to resize, sliders/buttons to customise
- Settings saved to `config/krypton_regionmap.json`
  - `density` = 81 values (row-major, row 0 = Z -225k, col 0 = X -225k). Tune these to refine the heatmap.

## Blur
The glass look uses only gradients/translucent fills. There is **no blur shader**, and the edit screen overrides
`renderBackground` so vanilla's menu blur is never requested. Nothing here can clash with another blur.

## Build
Push to GitHub. The Actions workflow (`.github/workflows/build.yml`) builds the jar:
Actions tab -> latest run -> Artifacts -> `krypton-regionmap`.
Locally: `gradle build` (Gradle 9.2+, JDK 21) -> `build/libs/krypton-regionmap-1.0.0.jar`.

## Targets
Fabric, Minecraft 1.21.11 (change versions in `gradle.properties`).
To hook into Krypton's own module system, call `RegionMapRenderer.render(drawContext, RegionMapMod.CONFIG, false)` from its HUD render event.
