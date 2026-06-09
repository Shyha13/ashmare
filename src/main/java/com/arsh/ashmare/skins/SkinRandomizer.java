package com.arsh.ashmare.skins;

import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.exclusions.ExclusionManager;
import com.arsh.ashmare.owners.OwnerManager;
import com.arsh.ashmare.presentation.PlayerProfilePresentation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SkinRandomizer {
	private static final int MAX_CONCURRENT_REQUESTS = 4;
	private static final ExecutorService HTTP_EXECUTOR =
			Executors.newFixedThreadPool(
					MAX_CONCURRENT_REQUESTS,
					Thread.ofPlatform()
							.daemon(true)
							.name("ashmare-skin-http-", 0)
							.factory()
			);
	private static final AtomicBoolean RANDOMIZATION_RUNNING =
			new AtomicBoolean();

	private SkinRandomizer() {
	}

	public static Optional<CompletableFuture<SkinRandomizationResult>> randomize(
			MinecraftServer server
	) {
		Objects.requireNonNull(server, "server");
		if (!RANDOMIZATION_RUNNING.compareAndSet(false, true)) {
			return Optional.empty();
		}

		try {
			AshmareConfig.skinUsernames().load();
			SkinSourceParser.Result sourceList = SkinSourceParser.parse(
					AshmareConfig.skinUsernames().get()
			);
			if (sourceList.usernames().isEmpty()) {
				RANDOMIZATION_RUNNING.set(false);
				return Optional.of(CompletableFuture.completedFuture(
						SkinRandomizationResult.failure(
								"No usable skin usernames were found in "
										+ AshmareConfig.skinUsernames().path()
										+ ". Add real Minecraft Java usernames "
										+ "one per line, then run the command again.",
								sourceList.invalidSources()
						)
				));
			}

			List<PlayerIdentity> players = server.getPlayerList()
					.getPlayers()
					.stream()
					.filter(player ->
							!ExclusionManager.isExcluded(player.getUUID())
									&& !OwnerManager.isOwner(player)
					)
					.map(PlayerIdentity::from)
					.toList();
			if (players.isEmpty()) {
				RANDOMIZATION_RUNNING.set(false);
				return Optional.of(CompletableFuture.completedFuture(
						SkinRandomizationResult.failure(
								"No eligible online players to randomize.",
								sourceList.invalidSources()
						)
				));
			}

			SkinsConfig configSnapshot = AshmareConfig.skins().get();
			CompletableFuture<SkinRandomizationResult> result =
					new CompletableFuture<>();

			resolveAll(sourceList.usernames(), configSnapshot)
					.whenComplete((batch, throwable) ->
							server.execute(() -> {
								try {
									if (throwable != null) {
										result.completeExceptionally(throwable);
										return;
									}
									result.complete(commitAssignments(
											server,
											players,
											sourceList.invalidSources(),
											batch
									));
								} catch (RuntimeException exception) {
									result.completeExceptionally(exception);
								} finally {
									RANDOMIZATION_RUNNING.set(false);
								}
							})
					);
			return Optional.of(result);
		} catch (RuntimeException exception) {
			RANDOMIZATION_RUNNING.set(false);
			return Optional.of(CompletableFuture.failedFuture(exception));
		}
	}

	public static boolean clear(MinecraftServer server, UUID playerUuid) {
		boolean cleared = AshmareConfig.skins().updateAndGet(
				config -> config.remove(playerUuid)
		);
		if (cleared) {
			PlayerProfilePresentation.refresh(server, List.of(playerUuid));
		}
		return cleared;
	}

	public static int clearAll(MinecraftServer server) {
		List<UUID> assignedUuids = AshmareConfig.skins()
				.get()
				.assignments()
				.stream()
				.map(SkinAssignment::playerUuid)
				.toList();
		int cleared = AshmareConfig.skins().updateAndGet(SkinsConfig::clear);
		PlayerProfilePresentation.refresh(server, assignedUuids);
		return cleared;
	}

	private static CompletableFuture<ResolutionBatch> resolveAll(
			List<String> usernames,
			SkinsConfig configSnapshot
	) {
		List<CompletableFuture<SourceOutcome>> requests = usernames.stream()
				.map(username -> CompletableFuture.supplyAsync(
						() -> resolveOne(username, configSnapshot),
						HTTP_EXECUTOR
				))
				.toList();

		return CompletableFuture.allOf(
						requests.toArray(CompletableFuture[]::new)
				)
				.thenApply(ignored -> {
					List<CachedSkin> resolved = new ArrayList<>();
					List<String> failures = new ArrayList<>();
					List<String> warnings = new ArrayList<>();
					for (CompletableFuture<SourceOutcome> request : requests) {
						SourceOutcome outcome = request.join();
						if (outcome.skin() != null) {
							resolved.add(outcome.skin());
						}
						if (outcome.failure() != null) {
							failures.add(outcome.failure());
						}
						if (outcome.warning() != null) {
							warnings.add(outcome.warning());
						}
					}
					return new ResolutionBatch(resolved, failures, warnings);
				});
	}

	private static SourceOutcome resolveOne(
			String username,
			SkinsConfig configSnapshot
	) {
		try {
			MojangSkinClient.SkinResolution resolution =
					MojangSkinClient.resolve(
							username,
							configSnapshot.findCached(username)
					);
			return new SourceOutcome(
					resolution.skin(),
					null,
					resolution.warning()
			);
		} catch (SkinApiException exception) {
			return new SourceOutcome(
					null,
					username + ": " + exception.getMessage(),
					null
			);
		}
	}

	private static SkinRandomizationResult commitAssignments(
			MinecraftServer server,
			List<PlayerIdentity> requestedPlayers,
			List<String> invalidSources,
			ResolutionBatch batch
	) {
		List<String> failures = new ArrayList<>(invalidSources);
		failures.addAll(batch.failures());
		if (batch.skins().isEmpty()) {
			return SkinRandomizationResult.failure(
					"Could not resolve any usable skin textures.",
					failures
			);
		}

		List<PlayerIdentity> eligiblePlayers = requestedPlayers.stream()
				.filter(player ->
						!ExclusionManager.isExcluded(player.uuid())
								&& !OwnerManager.isOwner(
										player.uuid(),
										player.username()
								)
				)
				.toList();
		if (eligiblePlayers.isEmpty()) {
			AshmareConfig.skins().update(config ->
					batch.skins().forEach(config::putCached)
			);
			return SkinRandomizationResult.failure(
					"No eligible players remained when skin lookup completed.",
					failures
			);
		}

		List<CachedSkin> shuffledSkins = new ArrayList<>(batch.skins());
		Collections.shuffle(shuffledSkins);
		Map<String, String> assignments = AshmareConfig.skins().updateAndGet(
				config -> {
					batch.skins().forEach(config::putCached);
					Set<UUID> playerUuids = eligiblePlayers.stream()
							.map(PlayerIdentity::uuid)
							.collect(java.util.stream.Collectors.toSet());
					config.removeAll(playerUuids);

					Map<String, String> assigned = new LinkedHashMap<>();
					for (int index = 0; index < eligiblePlayers.size(); index++) {
						PlayerIdentity player = eligiblePlayers.get(index);
						CachedSkin skin = shuffledSkins.get(
								index % shuffledSkins.size()
						);
						config.put(new SkinAssignment(
								player.uuid(),
								player.username(),
								skin.sourceUuid(),
								skin.sourceUsername(),
								skin.texture()
						));
						assigned.put(player.username(), skin.sourceUsername());
					}
					return Map.copyOf(assigned);
				}
		);
		PlayerProfilePresentation.refresh(
				server,
				eligiblePlayers.stream().map(PlayerIdentity::uuid).toList()
		);

		return new SkinRandomizationResult(
				assignments,
				batch.skins().size(),
				failures,
				batch.warnings(),
				null
		);
	}

	private record PlayerIdentity(UUID uuid, String username) {
		static PlayerIdentity from(ServerPlayer player) {
			return new PlayerIdentity(
					player.getUUID(),
					player.getGameProfile().name()
			);
		}
	}

	private record SourceOutcome(
			CachedSkin skin,
			String failure,
			String warning
	) {
	}

	private record ResolutionBatch(
			List<CachedSkin> skins,
			List<String> failures,
			List<String> warnings
	) {
	}
}
