package com.arsh.ashmare.mixin;

import com.arsh.ashmare.chat.ChatControl;
import com.arsh.ashmare.names.NamePresentation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@ModifyVariable(
			method = "broadcastSystemMessage"
					+ "(Lnet/minecraft/network/chat/Component;Z)V",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0
	)
	private Component ashmare$hideFormerRealName(Component message) {
		return NamePresentation.sanitizeSystemMessage(message);
	}

	@Inject(method = "broadcastSystemToTeam", at = @At("HEAD"), cancellable = true)
	private void ashmare$hideTeamDeathMessage(
			Player source,
			Component message,
			CallbackInfo callbackInfo
	) {
		if (ChatControl.shouldSuppressTeamDeathMessage(message)) {
			callbackInfo.cancel();
		}
	}

	@Inject(
			method = "broadcastSystemToAllExceptTeam",
			at = @At("HEAD"),
			cancellable = true
	)
	private void ashmare$hideOtherTeamDeathMessage(
			Player source,
			Component message,
			CallbackInfo callbackInfo
	) {
		if (ChatControl.shouldSuppressTeamDeathMessage(message)) {
			callbackInfo.cancel();
		}
	}
}
