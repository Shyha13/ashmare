package com.arsh.ashmare.owners;

import com.arsh.ashmare.AshmareMod;
import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.deathban.DeathbanManager;
import com.arsh.ashmare.names.NameRandomizer;
import com.arsh.ashmare.skins.SkinRandomizer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OwnerManager {
	private static final ExecutorService LOOKUP_EXECUTOR =
			Executors.newSingleThreadExecutor(
					Thread.ofPlatform()
							.daemon(true)
							.name("ashmare-owner-lookup")
							.factory()
			);

	private static MinecraftServer activeServer;

	private OwnerManager() {
	}

	public static void start(MinecraftServer server) {
		activeServer = Objects.requireNonNull(server, "server");
		owners().stream()
				.filter(OwnerEntry::isResolved)
				.forEach(owner -> enforceOwnerBypasses(server, owner.uuid()));
		resolveUnresolvedOwners(server);
	}

	public static void stop(MinecraftServer server) {
		if (activeServer == server) {
			activeServer = null;
		}
	}

	public static boolean isOwner(ServerPlayer player) {
		Objects.requireNonNull(player, "player");
		return isOwner(
				player.getUUID(),
				player.getGameProfile().name()
		);
	}

	public static boolean isOwner(UUID uuid) {
		return AshmareConfig.owners().read(config -> config.isOwner(uuid));
	}

	public static boolean isOwner(UUID uuid, String username) {
		return AshmareConfig.owners().read(
				config -> config.isOwner(uuid, username)
		);
	}

	public static List<OwnerEntry> owners() {
		return AshmareConfig.owners().get().owners();
	}

	public static boolean addOwner(MinecraftServer server, NameAndId identity) {
		Objects.requireNonNull(server, "server");
		Objects.requireNonNull(identity, "identity");
		boolean added = AshmareConfig.owners().updateAndGet(config ->
				config.add(identity.id(), identity.name())
		);
		enforceOwnerBypasses(server, identity.id());
		return added;
	}

	public static boolean removeOwner(String usernameOrUuid) {
		return AshmareConfig.owners().updateAndGet(
				config -> config.remove(usernameOrUuid)
		);
	}

	public static void observePlayer(
			MinecraftServer server,
			ServerPlayer player
	) {
		Objects.requireNonNull(server, "server");
		Objects.requireNonNull(player, "player");
		UUID uuid = player.getUUID();
		String username = player.getGameProfile().name();
		Optional<OwnerEntry> owner = AshmareConfig.owners().read(
				config -> config.find(uuid, username)
		);
		if (owner.isEmpty()) {
			return;
		}

		OwnerEntry existing = owner.get();
		if (
				!uuid.equals(existing.uuid())
						|| !username.equals(existing.lastKnownUsername())
		) {
			AshmareConfig.owners().update(
					config -> config.observe(uuid, username)
			);
			AshmareMod.LOGGER.info(
					"Resolved or refreshed Ashmare owner {} as {}.",
					username,
					uuid
			);
		}
		enforceOwnerBypasses(server, uuid);
	}

	private static void resolveUnresolvedOwners(MinecraftServer server) {
		for (OwnerEntry owner : AshmareConfig.owners().get().unresolvedOwners()) {
			CompletableFuture.supplyAsync(
					() -> lookupOwner(server, owner.lastKnownUsername()),
					LOOKUP_EXECUTOR
			).whenComplete((resolved, throwable) -> {
				if (throwable != null) {
					AshmareMod.LOGGER.warn(
							"Could not resolve owner UUID for {}; "
									+ "the lookup will retry next startup.",
							owner.lastKnownUsername(),
							throwable
					);
					return;
				}
				if (resolved.isEmpty()) {
					AshmareMod.LOGGER.warn(
							"Could not resolve owner UUID for {}; "
									+ "the lookup will retry next startup.",
							owner.lastKnownUsername()
					);
					return;
				}

				server.executeIfPossible(() -> {
					if (activeServer != server) {
						return;
					}
					NameAndId identity = resolved.get();
					boolean updated = AshmareConfig.owners().updateAndGet(config ->
							config.resolve(
									owner.lastKnownUsername(),
									identity.id(),
									identity.name()
							)
					);
					if (updated) {
						enforceOwnerBypasses(server, identity.id());
						AshmareMod.LOGGER.info(
								"Resolved Ashmare owner {} as {}.",
								identity.name(),
								identity.id()
						);
					}
				});
			});
		}
	}

	private static Optional<NameAndId> lookupOwner(
			MinecraftServer server,
			String username
	) {
		return server.services()
				.profileRepository()
				.findProfileByName(username)
				.map(profile -> new NameAndId(profile.id(), profile.name()));
	}

	private static void enforceOwnerBypasses(
			MinecraftServer server,
			UUID uuid
	) {
		DeathbanManager.clearForOwner(uuid);
		if (AshmareConfig.names().read(config -> config.find(uuid).isPresent())) {
			NameRandomizer.clear(server, uuid);
		}
		if (AshmareConfig.skins().read(config -> config.find(uuid).isPresent())) {
			SkinRandomizer.clear(server, uuid);
		}
	}
}
