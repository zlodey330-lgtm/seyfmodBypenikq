package com.penikq.seyfmod;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Ui {
    private Ui() {}

    public static final int GREEN = 0xFF55E67A;
    public static final int ORANGE = 0xFFFFA93A;
    public static final int RED = 0xFFFF5A5A;

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public static void fillRound(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        c.fill(x1 + 2, y1, x2 - 2, y2, col);
        c.fill(x1 + 1, y1 + 1, x1 + 2, y2 - 1, col);
        c.fill(x2 - 2, y1 + 1, x2 - 1, y2 - 1, col);
        c.fill(x1, y1 + 2, x1 + 1, y2 - 2, col);
        c.fill(x2 - 1, y1 + 2, x2, y2 - 2, col);
    }

    /** Тёмная панель с тенью и тонкой рамкой. */
    public static void panel(DrawContext c, int x, int y, int w, int h) {
        c.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x66000000);
        c.fillGradient(x, y, x + w, y + h, 0xF00A0D18, 0xF0151B30);
        c.drawBorder(x, y, w, h, 0xFF3A4A7A);
    }

    /** style: 0 = синяя, 1 = красная, 2 = зелёная */
    public static void button(DrawContext c, TextRenderer tr, int x, int y, int w, int h,
                              String label, boolean enabled, int style, int mx, int my) {
        boolean hover = enabled && inside(mx, my, x, y, w, h);
        int border, fill, text;
        if (!enabled) {
            border = 0xFF3B4466; fill = 0xFF232B45; text = 0xFF7A8299;
        } else if (style == 1) {
            border = hover ? 0xFFFF8A8A : 0xFFD05A5A;
            fill = hover ? 0xFFB23A3A : 0xFF8E2B2B;
            text = 0xFFFFFFFF;
        } else if (style == 2) {
            border = hover ? 0xFF8DF0A8 : 0xFF3FBF63;
            fill = hover ? 0xFF2F9E50 : 0xFF237A3D;
            text = 0xFFFFFFFF;
        } else {
            border = hover ? 0xFF8DB6FF : 0xFF5E94F0;
            fill = hover ? 0xFF2F6BD6 : 0xFF1E4FA8;
            text = 0xFFFFFFFF;
        }
        fillRound(c, x, y, x + w, y + h, border);
        fillRound(c, x + 1, y + 1, x + w - 1, y + h - 1, fill);
        c.drawCenteredTextWithShadow(tr, label, x + w / 2, y + (h - 8) / 2, text);
    }

    /** Квадратик 16x16. state: 0 = пусто, 1 = галочка, 2 = крестик. */
    public static void box(DrawContext c, int x, int y, int state, boolean hover) {
        int S = 16;
        int border = state == 1 ? GREEN : state == 2 ? RED : (hover ? 0xFF8DB6FF : 0xFF4A5A85);
        c.fill(x, y, x + S, y + S, border);
        c.fill(x + 1, y + 1, x + S - 1, y + S - 1, 0xFF0C1122);
        if (state == 1) {
            // галочка
            int[][] pts = {{3, 8}, {4, 9}, {5, 10}, {6, 11}, {7, 10}, {8, 9}, {9, 8}, {10, 7}, {11, 6}, {12, 5}};
            for (int[] p : pts) c.fill(x + p[0], y + p[1] - 1, x + p[0] + 2, y + p[1] + 1, GREEN);
        } else if (state == 2) {
            // крестик
            for (int i = 0; i < 10; i++) {
                c.fill(x + 3 + i, y + 3 + i, x + 5 + i, y + 5 + i, RED);
                c.fill(x + 3 + i, y + 12 - i, x + 5 + i, y + 14 - i, RED);
            }
        }
    }
}
