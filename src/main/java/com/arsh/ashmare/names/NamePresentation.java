package com.arsh.ashmare.names;

import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.exclusions.ExclusionManager;
import com.arsh.ashmare.owners.OwnerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NamePresentation {
	private NamePresentation() {
	}

	public static Optional<String> fakeName(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		if (
			ExclusionManager.isExcluded(uuid)
					|| OwnerManager.isOwner(uuid)
		) {
			return Optional.empty();
		}
		return AshmareConfig.names().read(config -> config.fakeName(uuid));
	}

	public static Optional<String> fakeName(UUID uuid, String username) {
		Objects.requireNonNull(uuid, "uuid");
		if (
			ExclusionManager.isExcluded(uuid)
					|| OwnerManager.isOwner(uuid, username)
		) {
			return Optional.empty();
		}
		return AshmareConfig.names().read(config -> config.fakeName(uuid));
	}

	public static Optional<String> fakeName(ServerPlayer player) {
		Objects.requireNonNull(player, "player");
		return fakeName(
				player.getUUID(),
				player.getGameProfile().name()
		);
	}

	public static Optional<Component> fakeDisplayName(ServerPlayer player) {
		return fakeName(player)
				.map(fakeName -> PlayerTeam.formatNameForTeam(
						player.getTeam(),
						Component.literal(fakeName)
				));
	}

	public static Component sanitizeSystemMessage(Component message) {
		if (!(message.getContents() instanceof TranslatableContents translatable)
				|| !translatable.getKey().equals("multiplayer.player.joined.renamed")) {
			return message;
		}

		Object[] arguments = translatable.getArgs();
		if (arguments.length == 0) {
			return message;
		}

		MutableComponent sanitized = Component.translatable(
				"multiplayer.player.joined",
				arguments[0]
		).withStyle(message.getStyle());
		message.getSiblings().forEach(sanitized::append);
		return sanitized;
	}
}
