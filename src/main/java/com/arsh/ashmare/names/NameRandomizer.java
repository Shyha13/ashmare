package com.arsh.ashmare.names;

import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.exclusions.ExclusionManager;
import com.arsh.ashmare.owners.OwnerManager;
import com.arsh.ashmare.presentation.PlayerProfilePresentation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class NameRandomizer {
	private NameRandomizer() {
	}

	public static Map<String, String> randomize(MinecraftServer server) {
		Objects.requireNonNull(server, "server");
		List<ServerPlayer> players = server.getPlayerList().getPlayers();

		List<ServerPlayer> eligiblePlayers = players.stream()
				.filter(player ->
						!ExclusionManager.isExcluded(player.getUUID())
								&& !OwnerManager.bypassesNameRandomization(player)
				)
				.toList();

		if (eligiblePlayers.isEmpty()) {
			return Map.of();
		}

		Map<String, String> generatedNames = AshmareConfig.names().updateAndGet(config -> {
			Set<UUID> randomizedUuids = eligiblePlayers.stream()
					.map(ServerPlayer::getUUID)
					.collect(java.util.stream.Collectors.toSet());
			config.removeAll(randomizedUuids);

			Set<String> usedNames = new HashSet<>();
			for (FakeNameAssignment assignment : config.assignments()) {
				usedNames.add(assignment.fakeName().toLowerCase(Locale.ROOT));
			}

			Map<String, String> assignments = new LinkedHashMap<>();
			for (ServerPlayer player : eligiblePlayers) {
				String fakeName = FakeNameGenerator.generateUnique(usedNames);
				usedNames.add(fakeName.toLowerCase(Locale.ROOT));

				String realUsername = player.getGameProfile().name();
				config.put(new FakeNameAssignment(
						player.getUUID(),
						realUsername,
						fakeName
				));
				assignments.put(realUsername, fakeName);
			}
			return Map.copyOf(assignments);
		});
		PlayerProfilePresentation.refresh(
				server,
				eligiblePlayers.stream().map(ServerPlayer::getUUID).toList()
		);
		return generatedNames;
	}

	public static boolean clear(MinecraftServer server, UUID uuid) {
		boolean cleared = AshmareConfig.names().updateAndGet(
				config -> config.remove(uuid)
		);
		if (cleared) {
			PlayerProfilePresentation.refresh(server, List.of(uuid));
		}
		return cleared;
	}

	public static int clearAll(MinecraftServer server) {
		List<UUID> assignedUuids = AshmareConfig.names()
				.get()
				.assignments()
				.stream()
				.map(FakeNameAssignment::uuid)
				.toList();
		int cleared = AshmareConfig.names().updateAndGet(NamesConfig::clear);
		PlayerProfilePresentation.refresh(server, assignedUuids);
		return cleared;
	}
}
