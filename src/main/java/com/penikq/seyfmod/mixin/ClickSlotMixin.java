package com.penikq.seyfmod.mixin;

import com.penikq.seyfmod.DropGuard;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClickSlotMixin {
    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void seyf$guard(int syncId, int slotId, int button, SlotActionType actionType,
                            PlayerEntity player, CallbackInfo ci) {
        if (DropGuard.interceptClick(syncId, slotId, button, actionType, player)) {
            ci.cancel();
        }
    }
}
