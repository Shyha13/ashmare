package com.arsh.ashmare.exclusions;

import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.deathban.DeathbanManager;
import com.arsh.ashmare.owners.OwnerManager;
import com.arsh.ashmare.presentation.PlayerProfilePresentation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ExclusionManager {
	private ExclusionManager() {
	}

	public static boolean isExcluded(UUID uuid) {
		return AshmareConfig.exclusions().read(config -> config.contains(uuid));
	}

	public static boolean blocksIdentityRandomization(ServerPlayer player) {
		Objects.requireNonNull(player, "player");
		return blocksIdentityRandomization(
				isExcluded(player.getUUID()),
				OwnerManager.isOwner(player)
		);
	}

	public static boolean blocksIdentityRandomization(
			UUID uuid,
			String username
	) {
		Objects.requireNonNull(uuid, "uuid");
		return blocksIdentityRandomization(
				isExcluded(uuid),
				OwnerManager.isOwner(uuid, username)
		);
	}

	public static boolean blocksIdentityRandomization(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		return blocksIdentityRandomization(
				isExcluded(uuid),
				OwnerManager.isOwner(uuid)
		);
	}

	static boolean blocksIdentityRandomization(
			boolean excluded,
			boolean owner
	) {
		return excluded && !owner;
	}

	public static boolean excludePlayer(ServerPlayer player) {
		Objects.requireNonNull(player, "player");

		boolean added = AshmareConfig.exclusions().updateAndGet(config -> config.exclude(
				player.getUUID(),
				player.getGameProfile().name()
		));
		DeathbanManager.clearForExclusion(player.getUUID());
		PlayerProfilePresentation.refresh(player.getUUID());
		return added;
	}

	public static boolean includePlayer(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		boolean removed = AshmareConfig.exclusions().updateAndGet(
				config -> config.include(uuid)
		);
		if (removed) {
			PlayerProfilePresentation.refresh(uuid);
		}
		return removed;
	}

	public static List<ExcludedPlayer> excludedPlayers() {
		return AshmareConfig.exclusions().get().players();
	}
}
