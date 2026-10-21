package com.penikq.seyfmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SeyfClient implements ClientModInitializer {
    public static KeyBinding OPEN_KEY;

    @Override
    public void onInitializeClient() {
        OPEN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seyfmodbypenikq.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.seyfmodbypenikq"
        ));
        SeyfConfig.get();

        // Открытие меню и перехват клавиши Q (когда экран не открыт)
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (OPEN_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new SafeScreen());
                }
            }
            if (client.currentScreen == null) {
                while (client.options.dropKey.wasPressed()) {
                    DropGuard.handleDropKey(client);
                }
            }
        });

        // Окно подтверждения поверх любого открытого экрана (инвентарь, сундук и т.д.)
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof DialogScreen) return;

            ScreenEvents.afterRender(screen).register((s, ctx, mouseX, mouseY, tickDelta) -> {
                if (Dialog.pending != null) {
                    Dialog.render(ctx, client.textRenderer, s.width, s.height, mouseX, mouseY);
                }
            });

            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
                if (Dialog.pending != null) {
                    Dialog.click(s.width, s.height, mouseX, mouseY, button);
                    return false;
                }
                return true;
            });

            ScreenKeyboardEvents.allowKeyPress(screen).register((s, key, scancode, modifiers) -> {
                if (Dialog.pending != null) {
                    if (key == GLFW.GLFW_KEY_ESCAPE) {
                        Dialog.cancel();
                    } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                        Dialog.confirm();
                    }
                    return false;
                }
                return true;
            });

            ScreenEvents.remove(screen).register(s -> {
                if (!(client.currentScreen instanceof DialogScreen)) Dialog.pending = null;
            });
        });
    }
}
