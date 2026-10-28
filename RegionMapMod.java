package dev.krypton.regionmap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RegionMapMod implements ClientModInitializer {
    public static RegionMapConfig CONFIG;
    private static KeyBinding editKey;

    @Override
    public void onInitializeClient() {
        CONFIG = RegionMapConfig.load();

        editKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.krypton_regionmap.edit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M,
                "key.categories.krypton_regionmap"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (editKey.wasPressed()) mc.setScreen(new RegionMapEditScreen(CONFIG));
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (!CONFIG.enabled || mc.options.hudHidden || mc.currentScreen instanceof RegionMapEditScreen) return;
            RegionMapRenderer.render(ctx, CONFIG, false);
        });
    }
}
