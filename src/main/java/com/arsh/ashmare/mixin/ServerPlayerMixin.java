package com.arsh.ashmare.mixin;

import com.arsh.ashmare.names.NamePresentation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Inject(
			method = "getTabListDisplayName",
			at = @At("HEAD"),
			cancellable = true
	)
	private void ashmare$useFakeTabName(
			CallbackInfoReturnable<Component> callback
	) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		NamePresentation.fakeName(player)
				.ifPresent(fakeName ->
						callback.setReturnValue(Component.literal(fakeName))
				);
	}
}
