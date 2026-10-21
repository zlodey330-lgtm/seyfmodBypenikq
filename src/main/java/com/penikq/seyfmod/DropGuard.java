package com.penikq.seyfmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

/** Вся логика перехвата выбрасывания. */
public final class DropGuard {
    private DropGuard() {}

    /** true, пока мы сами повторяем уже подтверждённое действие. */
    public static boolean bypass = false;

    public static String idOf(ItemStack st) {
        return Registries.ITEM.getId(st.getItem()).toString();
    }

    private static void blockedMessage(MinecraftClient mc, ItemStack st) {
        if (mc.inGameHud != null) {
            mc.inGameHud.setOverlayMessage(
                    Text.literal("Нельзя выбросить: " + st.getName().getString()).formatted(Formatting.RED), false);
        }
    }

    /** Выбрасывание через инвентарь (мышь, Q над слотом, клик за окном). true = отменить действие. */
    public static boolean interceptClick(int syncId, int slotId, int button, SlotActionType type, PlayerEntity player) {
        if (bypass) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || player != mc.player) return false;
        ScreenHandler h = player.currentScreenHandler;
        if (h == null || h.syncId != syncId) return false;

        ItemStack st;
        int count;
        if (type == SlotActionType.THROW && slotId >= 0 && slotId < h.slots.size()) {
            st = h.getSlot(slotId).getStack();
            count = button == 1 ? st.getCount() : Math.min(1, st.getCount());
        } else if (type == SlotActionType.PICKUP && slotId == ScreenHandler.EMPTY_SPACE_SLOT_INDEX) {
            st = h.getCursorStack();
            count = button == 1 ? Math.min(1, st.getCount()) : st.getCount();
        } else {
            return false;
        }
        if (st.isEmpty()) return false;

        int mode = SeyfConfig.get().modeOf(idOf(st));
        if (mode == SeyfConfig.NORMAL) return false;
        if (mode == SeyfConfig.BLOCKED) {
            blockedMessage(mc, st);
            return true;
        }
        if (Dialog.pending != null) return true;

        Dialog.request(mc, st.copy(), count, () -> {
            if (mc.interactionManager == null || mc.player == null) return;
            bypass = true;
            try {
                mc.interactionManager.clickSlot(syncId, slotId, button, type, mc.player);
            } finally {
                bypass = false;
            }
        });
        return true;
    }

    /** Выбрасывание клавишей Q из руки без открытого экрана. */
    public static void handleDropKey(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || p.isSpectator()) return;
        ItemStack st = p.getInventory().getMainHandStack();
        if (st.isEmpty()) return;
        boolean entire = Screen.hasControlDown();
        int mode = SeyfConfig.get().modeOf(idOf(st));

        if (mode == SeyfConfig.BLOCKED) {
            blockedMessage(mc, st);
            return;
        }
        if (mode == SeyfConfig.CONFIRM) {
            if (Dialog.pending != null) return;
            int count = entire ? st.getCount() : 1;
            Dialog.request(mc, st.copy(), count, () -> doDrop(mc, entire));
            return;
        }
        doDrop(mc, entire);
    }

    private static void doDrop(MinecraftClient mc, boolean entire) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;
        p.dropSelectedItem(entire);
        p.swingHand(Hand.MAIN_HAND);
    }
}
