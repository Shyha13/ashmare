package com.arsh.ashmare.mixin;

import com.arsh.ashmare.chat.ChatControl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {
	@Inject(method = "broadcastToAdmins", at = @At("HEAD"), cancellable = true)
	private void ashmare$hideCommandOutputFromOthers(
			Component message,
			CallbackInfo callbackInfo
	) {
		if (!ChatControl.commandOutputToOthersEnabled()) {
			callbackInfo.cancel();
		}
	}
}
