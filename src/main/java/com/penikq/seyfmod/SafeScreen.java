package com.penikq.seyfmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Главное меню: список предметов с защитой от выбрасывания. */
public class SafeScreen extends Screen {
    private static final int W = 320;
    private static final int ROW_H = 26;
    private static final int BH = 20;
    private static final int BTN_W = 145;

    private int px, py, ph;
    private int scroll = 0;
    private String status = "";
    private int statusTicks = 0;

    public SafeScreen() {
        super(Text.literal("seyfmodBypenikq"));
    }

    @Override
    protected void init() {
        ph = Math.min(320, this.height - 20);
        px = (this.width - W) / 2;
        py = (this.height - ph) / 2;
        clampScroll();
    }

    // ---- геометрия ----
    private int lx1() { return px + 8; }
    private int lx2() { return px + W - 8; }
    private int ly1() { return py + 68; }
    private int ly2() { return py + ph - 8; }
    private int removeX() { return lx2() - 52; }
    private int boxBX() { return removeX() - 22; }
    private int boxAX() { return boxBX() - 22; }
    private int addBtnX() { return px + 10; }
    private int handBtnX() { return px + 20 + BTN_W; }
    private int btnY() { return py + 24; }

    private List<String> ids() {
        return new ArrayList<>(SeyfConfig.get().items.keySet());
    }

    private int contentHeight() { return ids().size() * ROW_H + 4; }

    private void clampScroll() {
        int max = Math.max(0, contentHeight() - (ly2() - ly1()));
        scroll = Math.max(0, Math.min(scroll, max));
    }

    private static ItemStack stackFor(String id) {
        Identifier ident = Identifier.tryParse(id);
        if (ident != null && Registries.ITEM.containsId(ident)) {
            return new ItemStack(Registries.ITEM.get(ident));
        }
        return new ItemStack(Items.BARRIER);
    }

    private void flash(String text) {
        status = text;
        statusTicks = 50;
    }

    // ---- рисование ----
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x99000208);
        Ui.panel(ctx, px, py, W, ph);

        ctx.drawCenteredTextWithShadow(textRenderer, "seyfmodBypenikq", px + W / 2, py + 8, 0xFF8EC5FF);

        Ui.button(ctx, textRenderer, addBtnX(), btnY(), BTN_W, BH, "Добавить предмет", true, 0, mouseX, mouseY);
        Ui.button(ctx, textRenderer, handBtnX(), btnY(), BTN_W, BH, "Предмет в руке", true, 0, mouseX, mouseY);

        // легенда или статус
        int ly = py + 50;
        if (statusTicks > 0) {
            ctx.drawCenteredTextWithShadow(textRenderer, status, px + W / 2, ly, Ui.GREEN);
        } else {
            int x = px + 10;
            x = legend(ctx, x, ly, 1, "подтверждение");
            x = legend(ctx, x + 10, ly, 2, "запрет");
            legend(ctx, x + 10, ly, 0, "обычный");
        }

        // список
        int lx1 = lx1(), lx2 = lx2(), ly1 = ly1(), ly2 = ly2();
        ctx.fill(lx1, ly1, lx2, ly2, 0x55000208);

        List<String> ids = ids();
        String hoverTip = null;
        if (ids.isEmpty()) {
            int cy = ly1 + 40;
            ctx.drawCenteredTextWithShadow(textRenderer, "Список пуст.", px + W / 2, cy, 0xFFB5C8EE);
            ctx.drawCenteredTextWithShadow(textRenderer, "Добавь предмет, который нужно", px + W / 2, cy + 16, 0xFF8FA3C8);
            ctx.drawCenteredTextWithShadow(textRenderer, "защитить от выбрасывания.", px + W / 2, cy + 28, 0xFF8FA3C8);
        } else {
            ctx.enableScissor(lx1, ly1, lx2, ly2);
            for (int i = 0; i < ids.size(); i++) {
                int ry = ly1 + 2 + i * ROW_H - scroll;
                if (ry + ROW_H < ly1 || ry > ly2) continue;
                String id = ids.get(i);
                int mode = SeyfConfig.get().modeOf(id);
                ItemStack icon = stackFor(id);

                if (i % 2 == 1) ctx.fill(lx1, ry, lx2, ry + ROW_H, 0x1AFFFFFF);
                int accent = mode == SeyfConfig.CONFIRM ? Ui.GREEN : mode == SeyfConfig.BLOCKED ? Ui.RED : 0xFF4A5A85;
                ctx.fill(lx1, ry + 2, lx1 + 2, ry + ROW_H - 2, accent);
                ctx.drawItem(icon, lx1 + 6, ry + 5);

                String name = icon.isOf(Items.BARRIER) ? id : icon.getName().getString();
                name = textRenderer.trimToWidth(name, boxAX() - (lx1 + 28) - 6);
                ctx.drawTextWithShadow(textRenderer, name, lx1 + 28, ry + 9, 0xFFE6EEFF);

                int by = ry + 5;
                boolean inList = mouseY >= ly1 && mouseY < ly2;
                boolean hA = inList && Ui.inside(mouseX, mouseY, boxAX(), by, 16, 16);
                boolean hB = inList && Ui.inside(mouseX, mouseY, boxBX(), by, 16, 16);
                Ui.box(ctx, boxAX(), by, mode == SeyfConfig.CONFIRM ? 1 : 0, hA);
                Ui.box(ctx, boxBX(), by, mode == SeyfConfig.BLOCKED ? 2 : 0, hB);
                if (hA) hoverTip = "Выбрасывать с подтверждением";
                if (hB) hoverTip = "Нельзя выбросить вообще";

                Ui.button(ctx, textRenderer, removeX(), by, 46, 16, "Убрать", true, 1, mouseX, mouseY);
            }
            ctx.disableScissor();

            int visible = ly2 - ly1;
            int content = contentHeight();
            if (content > visible) {
                int barH = Math.max(14, visible * visible / content);
                int maxScroll = content - visible;
                int barY = ly1 + (int) ((long) (visible - barH) * scroll / maxScroll);
                ctx.fill(lx2 - 3, ly1, lx2 - 1, ly2, 0x33FFFFFF);
                ctx.fill(lx2 - 3, barY, lx2 - 1, barY + barH, 0xFF5E94F0);
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
        if (hoverTip != null) {
            ctx.drawTooltip(textRenderer, Text.literal(hoverTip), mouseX, mouseY);
        }
    }

    /** Рисует пункт легенды, возвращает x, где он закончился. */
    private int legend(DrawContext ctx, int x, int y, int state, String label) {
        // уменьшенный квадратик 10x10
        int s = 10;
        int border = state == 1 ? Ui.GREEN : state == 2 ? Ui.RED : 0xFF4A5A85;
        ctx.fill(x, y - 1, x + s, y - 1 + s, border);
        ctx.fill(x + 1, y, x + s - 1, y - 2 + s, 0xFF0C1122);
        if (state == 1) {
            ctx.fill(x + 2, y + 3, x + 4, y + 5, Ui.GREEN);
            ctx.fill(x + 4, y + 4, x + 6, y + 6, Ui.GREEN);
            ctx.fill(x + 6, y + 2, x + 8, y + 4, Ui.GREEN);
        } else if (state == 2) {
            for (int i = 0; i < 5; i++) {
                ctx.fill(x + 2 + i, y + 1 + i, x + 3 + i, y + 2 + i, Ui.RED);
                ctx.fill(x + 2 + i, y + 5 - i, x + 3 + i, y + 6 - i, Ui.RED);
            }
        }
        ctx.drawTextWithShadow(textRenderer, label, x + s + 4, y, 0xFF9FB0D8);
        return x + s + 4 + textRenderer.getWidth(label);
    }

    // ---- ввод ----
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (Ui.inside(mx, my, addBtnX(), btnY(), BTN_W, BH)) {
                client.setScreen(new PickerScreen(this));
                return true;
            }
            if (Ui.inside(mx, my, handBtnX(), btnY(), BTN_W, BH)) {
                addHeld();
                return true;
            }

            if (my >= ly1() && my < ly2()) {
                List<String> ids = ids();
                for (int i = 0; i < ids.size(); i++) {
                    int ry = ly1() + 2 + i * ROW_H - scroll;
                    if (my < ry || my >= ry + ROW_H) continue;
                    int by = ry + 5;
                    String id = ids.get(i);
                    int mode = SeyfConfig.get().modeOf(id);
                    if (Ui.inside(mx, my, boxAX(), by, 16, 16)) {
                        SeyfConfig.get().setMode(id, mode == SeyfConfig.CONFIRM ? SeyfConfig.NORMAL : SeyfConfig.CONFIRM);
                        return true;
                    }
                    if (Ui.inside(mx, my, boxBX(), by, 16, 16)) {
                        SeyfConfig.get().setMode(id, mode == SeyfConfig.BLOCKED ? SeyfConfig.NORMAL : SeyfConfig.BLOCKED);
                        return true;
                    }
                    if (Ui.inside(mx, my, removeX(), by, 46, 16)) {
                        SeyfConfig.get().remove(id);
                        clampScroll();
                        return true;
                    }
                    break;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private void addHeld() {
        if (client == null || client.player == null) return;
        ItemStack st = client.player.getMainHandStack();
        if (st.isEmpty()) {
            flash("В руке пусто");
            return;
        }
        String id = Registries.ITEM.getId(st.getItem()).toString();
        if (SeyfConfig.get().has(id)) {
            flash("Уже в списке: " + st.getName().getString());
        } else {
            SeyfConfig.get().setMode(id, SeyfConfig.CONFIRM);
            flash("Добавлено: " + st.getName().getString());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) (verticalAmount * 16);
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (SeyfClient.OPEN_KEY != null && SeyfClient.OPEN_KEY.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (statusTicks > 0) statusTicks--;
    }
}
