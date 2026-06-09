package com.arsh.ashmare.deathban;

import com.arsh.ashmare.AshmareMod;
import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.exclusions.ExclusionManager;
import com.arsh.ashmare.owners.OwnerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class DeathbanManager {
	private static final String BAN_SOURCE = "Ashmare";
	private static final String BAN_REASON = "Deathban";

	private static final ScheduledExecutorService SCHEDULER =
			Executors.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, "Ashmare Deathban Scheduler");
				thread.setDaemon(true);
				return thread;
			});

	private static final Map<UUID, ScheduledFuture<?>> SCHEDULED_TASKS = new HashMap<>();
	private static MinecraftServer server;

	private DeathbanManager() {
	}

	public static void start(MinecraftServer minecraftServer) {
		server = Objects.requireNonNull(minecraftServer, "minecraftServer");
		cancelAllTasks();

		long now = System.currentTimeMillis();
		for (DeathbanEntry entry : entries()) {
			if (
				ExclusionManager.isExcluded(entry.uuid())
						|| OwnerManager.isOwner(
								entry.uuid(),
								entry.lastKnownUsername()
						)
			) {
				removeVanillaBan(entry);
				removeEntry(entry.uuid());
				continue;
			}

			if (entry.isExpired(now)) {
				removeVanillaBan(entry);
				removeEntry(entry.uuid());
			} else if (
					entry.status() == DeathbanStatus.PENDING
							&& entry.banAtEpochMillis() <= now
			) {
				applyDeathban(entry.uuid());
			} else {
				if (entry.status() == DeathbanStatus.ACTIVE) {
					ensureVanillaBan(entry);
				}
				schedule(entry);
			}
		}
	}

	public static void stop(MinecraftServer minecraftServer) {
		if (server == minecraftServer) {
			cancelAllTasks();
			server = null;
		}
	}

	public static void onPlayerDeath(ServerPlayer player) {
		Objects.requireNonNull(player, "player");
		if (
			ExclusionManager.isExcluded(player.getUUID())
					|| OwnerManager.isOwner(player)
		) {
			return;
		}

		DeathbanConfig config = AshmareConfig.deathban().get();
		long diedAt = System.currentTimeMillis();
		long banAt = diedAt + (long) config.delaySeconds() * 1_000L;
		DeathbanDuration duration = config.duration();

		DeathbanEntry entry = new DeathbanEntry(
				player.getUUID(),
				player.getGameProfile().name(),
				diedAt,
				banAt,
				duration.configValue(),
				duration.expirationFrom(banAt),
				DeathbanStatus.PENDING
		);

		AshmareConfig.deathban().update(deathbanConfig -> deathbanConfig.upsert(entry));
		schedule(entry);
		AshmareMod.LOGGER.info(
				"Scheduled deathban for {} ({}) in {} seconds.",
				entry.lastKnownUsername(),
				entry.uuid(),
				config.delaySeconds()
		);
	}

	public static List<DeathbanEntry> entries() {
		return AshmareConfig.deathban().get().activeDeathbans();
	}

	public static boolean unban(MinecraftServer minecraftServer, String usernameOrUuid) {
		Optional<DeathbanEntry> matchingEntry = AshmareConfig.deathban()
				.get()
				.find(usernameOrUuid);

		if (matchingEntry.isEmpty()) {
			return false;
		}

		DeathbanEntry entry = matchingEntry.get();
		cancelTask(entry.uuid());
		AshmareConfig.deathban().update(config -> config.remove(entry.uuid()));
		removeVanillaBan(minecraftServer, entry);
		return true;
	}

	public static boolean clearForExclusion(UUID uuid) {
		return clearBypass(uuid);
	}

	public static boolean clearForOwner(UUID uuid) {
		return clearBypass(uuid);
	}

	private static boolean clearBypass(UUID uuid) {
		Optional<DeathbanEntry> entry = AshmareConfig.deathban().get().find(uuid);
		if (entry.isEmpty()) {
			return false;
		}

		cancelTask(uuid);
		AshmareConfig.deathban().update(config -> config.remove(uuid));
		removeVanillaBan(entry.get());
		return true;
	}

	private static void schedule(DeathbanEntry entry) {
		if (server == null) {
			return;
		}

		cancelTask(entry.uuid());
		long targetTime = entry.status() == DeathbanStatus.PENDING
				? entry.banAtEpochMillis()
				: entry.expiresAtEpochMillis() == null
						? Long.MAX_VALUE
						: entry.expiresAtEpochMillis();

		if (targetTime == Long.MAX_VALUE) {
			return;
		}

		long delayMillis = Math.max(0L, targetTime - System.currentTimeMillis());
		MinecraftServer scheduledServer = server;
		ScheduledFuture<?> future = SCHEDULER.schedule(
				() -> scheduledServer.executeIfPossible(() -> runScheduled(entry.uuid())),
				delayMillis,
				TimeUnit.MILLISECONDS
		);
		SCHEDULED_TASKS.put(entry.uuid(), future);
	}

	private static void runScheduled(UUID uuid) {
		SCHEDULED_TASKS.remove(uuid);
		Optional<DeathbanEntry> entry = AshmareConfig.deathban().get().find(uuid);
		if (entry.isEmpty()) {
			return;
		}

		if (entry.get().isExpired(System.currentTimeMillis())) {
			removeVanillaBan(entry.get());
			removeEntry(uuid);
		} else if (entry.get().status() == DeathbanStatus.PENDING) {
			applyDeathban(uuid);
		}
	}

	private static void applyDeathban(UUID uuid) {
		if (server == null) {
			return;
		}

		Optional<DeathbanEntry> matchingEntry = AshmareConfig.deathban().get().find(uuid);
		if (matchingEntry.isEmpty()) {
			return;
		}

		DeathbanEntry entry = matchingEntry.get();
		if (
			ExclusionManager.isExcluded(uuid)
					|| OwnerManager.isOwner(
							uuid,
							entry.lastKnownUsername()
					)
		) {
			removeEntry(uuid);
			return;
		}

		if (entry.status() == DeathbanStatus.PENDING) {
			ensureVanillaBan(entry);
			DeathbanEntry activeEntry = entry.activate();
			AshmareConfig.deathban().update(config -> config.upsert(activeEntry));
			ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
			if (onlinePlayer != null) {
				onlinePlayer.connection.disconnect(
						Component.literal("You have been deathbanned.")
				);
			}
			schedule(activeEntry);

			AshmareMod.LOGGER.info(
					"Applied deathban to {} ({}), duration {}.",
					entry.lastKnownUsername(),
					uuid,
					entry.duration()
			);
		}
	}

	private static void ensureVanillaBan(DeathbanEntry entry) {
		if (server == null) {
			return;
		}

		NameAndId identity = identity(entry);
		if (server.getPlayerList().getBans().isBanned(identity)) {
			return;
		}

		Date created = new Date(entry.diedAtEpochMillis());
		Date expires = entry.expiresAtEpochMillis() == null
				? null
				: new Date(entry.expiresAtEpochMillis());

		server.getPlayerList().getBans().add(new UserBanListEntry(
				identity,
				created,
				BAN_SOURCE,
				expires,
				BAN_REASON
		));
	}

	private static NameAndId identity(DeathbanEntry entry) {
		return new NameAndId(entry.uuid(), entry.lastKnownUsername());
	}

	private static void removeVanillaBan(DeathbanEntry entry) {
		if (server != null) {
			removeVanillaBan(server, entry);
		}
	}

	private static void removeVanillaBan(
			MinecraftServer minecraftServer,
			DeathbanEntry entry
	) {
		NameAndId identity = identity(entry);
		UserBanListEntry vanillaEntry = minecraftServer.getPlayerList().getBans().get(identity);

		if (
				vanillaEntry != null
						&& BAN_SOURCE.equals(vanillaEntry.getSource())
						&& BAN_REASON.equals(vanillaEntry.getReason())
		) {
			minecraftServer.getPlayerList().getBans().remove(identity);
		}
	}

	private static boolean removeEntry(UUID uuid) {
		cancelTask(uuid);
		return AshmareConfig.deathban().updateAndGet(config -> config.remove(uuid));
	}

	private static void cancelTask(UUID uuid) {
		ScheduledFuture<?> task = SCHEDULED_TASKS.remove(uuid);
		if (task != null) {
			task.cancel(false);
		}
	}

	private static void cancelAllTasks() {
		SCHEDULED_TASKS.values().forEach(task -> task.cancel(false));
		SCHEDULED_TASKS.clear();
	}
}
