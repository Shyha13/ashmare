package com.arsh.ashmare.mixin;

import com.arsh.ashmare.presentation.PlayerProfilePresentation;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public abstract class PlayerInfoPacketMixin {
	@Shadow
	@Final
	@Mutable
	private List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

	@Inject(
			method = "<init>(Ljava/util/EnumSet;Ljava/util/Collection;)V",
			at = @At("RETURN")
	)
	private void ashmare$rewritePlayerProfiles(
			EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions,
			Collection<ServerPlayer> players,
			CallbackInfo callback
	) {
		rewriteEntries();
	}

	@Inject(
			method = "<init>(Lnet/minecraft/network/protocol/game/"
					+ "ClientboundPlayerInfoUpdatePacket$Action;"
					+ "Lnet/minecraft/server/level/ServerPlayer;)V",
			at = @At("RETURN")
	)
	private void ashmare$rewriteSinglePlayerProfile(
			ClientboundPlayerInfoUpdatePacket.Action action,
			ServerPlayer player,
			CallbackInfo callback
	) {
		rewriteEntries();
	}

	private void rewriteEntries() {
		entries = entries.stream()
				.map(PlayerProfilePresentation::rewriteEntry)
				.toList();
	}
}
