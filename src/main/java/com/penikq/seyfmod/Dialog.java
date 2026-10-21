package com.penikq.seyfmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Маленькое тёмное окно подтверждения «Выбросить?». */
public final class Dialog {
    private Dialog() {}

    public static class Pending {
        public final ItemStack stack;
        public final int count;
        public final Runnable onConfirm;

        public Pending(ItemStack stack, int count, Runnable onConfirm) {
            this.stack = stack;
            this.count = count;
            this.onConfirm = onConfirm;
        }
    }

    public static Pending pending;

    private static final int DW = 230;
    private static final int DH = 92;
    private static final int BW = 100;
    private static final int BH = 20;

    /** Показать окно. Если сейчас нет открытого экрана, открываем свой. */
    public static void request(MinecraftClient mc, ItemStack stack, int count, Runnable onConfirm) {
        pending = new Pending(stack, count, onConfirm);
        if (mc.currentScreen == null) {
            mc.setScreen(new DialogScreen());
        }
    }

    public static void render(DrawContext ctx, TextRenderer tr, int sw, int sh, int mx, int my) {
        Pending p = pending;
        if (p == null) return;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, 0, 600);

        ctx.fill(0, 0, sw, sh, 0xAA000000);
        int dx = (sw - DW) / 2, dy = (sh - DH) / 2;
        Ui.panel(ctx, dx, dy, DW, DH);

        ctx.drawCenteredTextWithShadow(tr, "Предупреждение", dx + DW / 2, dy + 8, Ui.ORANGE);
        ctx.drawItem(p.stack, dx + 14, dy + 28);
        String name = tr.trimToWidth(p.stack.getName().getString(), DW - 60);
        ctx.drawTextWithShadow(tr, name, dx + 38, dy + 27, 0xFFE6EEFF);
        ctx.drawTextWithShadow(tr, "Выбросить: " + p.count + " шт.", dx + 38, dy + 40, 0xFF9FB0D8);

        int by = dy + DH - BH - 10;
        Ui.button(ctx, tr, dx + 12, by, BW, BH, "Подтвердить", true, 2, mx, my);
        Ui.button(ctx, tr, dx + DW - 12 - BW, by, BW, BH, "Отклонить", true, 1, mx, my);

        ctx.getMatrices().pop();
    }

    public static void click(int sw, int sh, double mx, double my, int button) {
        if (pending == null || button != 0) return;
        int dx = (sw - DW) / 2, dy = (sh - DH) / 2;
        int by = dy + DH - BH - 10;
        if (Ui.inside(mx, my, dx + 12, by, BW, BH)) {
            confirm();
        } else if (Ui.inside(mx, my, dx + DW - 12 - BW, by, BW, BH)) {
            cancel();
        }
    }

    public static void confirm() {
        Pending p = pending;
        pending = null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen instanceof DialogScreen) mc.setScreen(null);
        if (p != null) p.onConfirm.run();
    }

    public static void cancel() {
        pending = null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen instanceof DialogScreen) mc.setScreen(null);
    }
}
