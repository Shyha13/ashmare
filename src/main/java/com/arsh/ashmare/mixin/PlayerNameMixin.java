package com.arsh.ashmare.mixin;

import com.arsh.ashmare.names.NamePresentation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerNameMixin {
	@Inject(method = "getName", at = @At("HEAD"), cancellable = true)
	private void ashmare$useFakeDisplayName(
			CallbackInfoReturnable<Component> callback
	) {
		if ((Object) this instanceof ServerPlayer player) {
			NamePresentation.fakeName(player)
					.ifPresent(fakeName ->
							callback.setReturnValue(Component.literal(fakeName))
					);
		}
	}

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void ashmare$useCleanFakeDisplayName(
			CallbackInfoReturnable<Component> callback
	) {
		if ((Object) this instanceof ServerPlayer player) {
			NamePresentation.fakeDisplayName(player)
					.ifPresent(callback::setReturnValue);
		}
	}
}
