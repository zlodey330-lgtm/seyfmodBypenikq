package com.penikq.seyfmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Выбор любого предмета из игры: поиск по названию и сетка предметов. */
public class PickerScreen extends Screen {
    private static final int W = 320;
    private static final int CELL = 24;
    private static final int COLS = 12;
    private static final int BH = 20;

    private static class Entry {
        final ItemStack stack;
        final String id;
        final String lower;

        Entry(ItemStack stack, String id, String lower) {
            this.stack = stack;
            this.id = id;
            this.lower = lower;
        }
    }

    private static List<Entry> ALL;

    private final Screen parent;
    private TextFieldWidget search;
    private final List<Entry> filtered = new ArrayList<>();
    private int px, py, ph;
    private int scroll = 0;
    private String status = "";
    private int statusTicks = 0;

    public PickerScreen(Screen parent) {
        super(Text.literal("Выбор предмета"));
        this.parent = parent;
    }

    private static List<Entry> all() {
        if (ALL == null) {
            List<Entry> list = new ArrayList<>();
            for (Item item : Registries.ITEM) {
                if (item == Items.AIR) continue;
                ItemStack st = new ItemStack(item);
                String id = Registries.ITEM.getId(item).toString();
                String lower = (st.getName().getString() + " " + id).toLowerCase(Locale.ROOT);
                list.add(new Entry(st, id, lower));
            }
            ALL = list;
        }
        return ALL;
    }

    @Override
    protected void init() {
        ph = Math.min(340, this.height - 20);
        px = (this.width - W) / 2;
        py = (this.height - ph) / 2;

        search = new TextFieldWidget(textRenderer, px + 10, py + 24, W - 20, 18, Text.literal("Поиск"));
        search.setMaxLength(64);
        search.setChangedListener(this::applyFilter);
        this.addDrawableChild(search);
        this.setInitialFocus(search);
        applyFilter(search.getText());
    }

    private void applyFilter(String q) {
        filtered.clear();
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        for (Entry e : all()) {
            if (needle.isEmpty() || e.lower.contains(needle)) filtered.add(e);
        }
        scroll = 0;
    }

    // ---- геометрия ----
    private int gx() { return px + (W - COLS * CELL) / 2; }
    private int gy1() { return py + 50; }
    private int gy2() { return py + ph - 34; }
    private int backX() { return px + (W - 120) / 2; }
    private int backY() { return py + ph - 28; }

    private int contentHeight() {
        int rows = (filtered.size() + COLS - 1) / COLS;
        return rows * CELL;
    }

    private void clampScroll() {
        int max = Math.max(0, contentHeight() - (gy2() - gy1()));
        scroll = Math.max(0, Math.min(scroll, max));
    }

    // ---- рисование ----
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x99000208);
        Ui.panel(ctx, px, py, W, ph);
        ctx.drawCenteredTextWithShadow(textRenderer, "Выбери предмет", px + W / 2, py + 8, 0xFF8EC5FF);

        super.render(ctx, mouseX, mouseY, delta);
        if (search.getText().isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "Поиск предмета...", search.getX() + 5, search.getY() + 5, 0xFF6F7C9E);
        }

        int gx = gx(), gy1 = gy1(), gy2 = gy2();
        ctx.fill(px + 8, gy1, px + W - 8, gy2, 0x55000208);

        ItemStack hovered = null;
        clampScroll();
        ctx.enableScissor(px + 8, gy1, px + W - 8, gy2);
        for (int i = 0; i < filtered.size(); i++) {
            int row = i / COLS, col = i % COLS;
            int x = gx + col * CELL;
            int y = gy1 + row * CELL - scroll;
            if (y + CELL < gy1 || y > gy2) continue;
            Entry e = filtered.get(i);
            boolean hover = mouseY >= gy1 && mouseY < gy2 && Ui.inside(mouseX, mouseY, x, y, CELL, CELL);
            if (hover) {
                ctx.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x663C6FD1);
                hovered = e.stack;
            }
            if (SeyfConfig.get().has(e.id)) {
                ctx.drawBorder(x + 1, y + 1, CELL - 2, CELL - 2, Ui.GREEN);
            }
            ctx.drawItem(e.stack, x + 4, y + 4);
        }
        ctx.disableScissor();

        int visible = gy2 - gy1;
        int content = contentHeight();
        if (content > visible) {
            int barH = Math.max(14, visible * visible / content);
            int maxScroll = content - visible;
            int barY = gy1 + (int) ((long) (visible - barH) * scroll / maxScroll);
            ctx.fill(px + W - 11, gy1, px + W - 9, gy2, 0x33FFFFFF);
            ctx.fill(px + W - 11, barY, px + W - 9, barY + barH, 0xFF5E94F0);
        }

        if (filtered.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Ничего не найдено", px + W / 2, gy1 + 30, 0xFF8FA3C8);
        }

        if (statusTicks > 0) {
            ctx.drawCenteredTextWithShadow(textRenderer, status, px + W / 2, gy2 + 6, Ui.GREEN);
        }

        Ui.button(ctx, textRenderer, backX(), backY(), 120, BH, "Назад", true, 0, mouseX, mouseY);

        if (hovered != null) {
            ctx.drawItemTooltip(textRenderer, hovered, mouseX, mouseY);
        }
    }

    // ---- ввод ----
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (Ui.inside(mx, my, backX(), backY(), 120, BH)) {
                this.close();
                return true;
            }
            if (my >= gy1() && my < gy2()) {
                int col = (int) ((mx - gx()) / CELL);
                int row = (int) ((my - gy1() + scroll) / CELL);
                if (mx >= gx() && col >= 0 && col < COLS && row >= 0) {
                    int idx = row * COLS + col;
                    if (idx >= 0 && idx < filtered.size()) {
                        Entry e = filtered.get(idx);
                        if (SeyfConfig.get().has(e.id)) {
                            status = "Уже в списке: " + e.stack.getName().getString();
                        } else {
                            SeyfConfig.get().setMode(e.id, SeyfConfig.CONFIRM);
                            status = "Добавлено: " + e.stack.getName().getString();
                        }
                        statusTicks = 50;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) (verticalAmount * 24);
        clampScroll();
        return true;
    }

    @Override
    public void tick() {
        if (statusTicks > 0) statusTicks--;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
