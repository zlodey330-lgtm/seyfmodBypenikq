package com.penikq.seyfmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Экран окна подтверждения, когда предмет выбрасывают клавишей Q без открытого инвентаря. */
public class DialogScreen extends Screen {
    public DialogScreen() {
        super(Text.literal("Предупреждение"));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        Dialog.render(ctx, textRenderer, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Dialog.click(width, height, mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Dialog.cancel();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            Dialog.confirm();
            return true;
        }
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        // если экран закрыли не кнопкой, окно подтверждения тоже сбрасываем
        Dialog.pending = null;
    }
}
