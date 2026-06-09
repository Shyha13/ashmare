package com.arsh.ashmare.exclusions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ExclusionsConfig {
	private List<ExcludedPlayer> players = new ArrayList<>();

	public List<ExcludedPlayer> players() {
		ensurePlayers();
		return List.copyOf(players);
	}

	public boolean contains(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensurePlayers();
		return players.stream().anyMatch(player -> player.uuid().equals(uuid));
	}

	public boolean exclude(UUID uuid, String lastKnownUsername) {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(lastKnownUsername, "lastKnownUsername");
		ensurePlayers();

		boolean wasExcluded = players.removeIf(player -> player.uuid().equals(uuid));
		players.add(new ExcludedPlayer(uuid, lastKnownUsername));
		players.sort(Comparator.comparing(
				ExcludedPlayer::lastKnownUsername,
				String.CASE_INSENSITIVE_ORDER
		));
		return !wasExcluded;
	}

	public boolean include(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensurePlayers();
		return players.removeIf(player -> player.uuid().equals(uuid));
	}

	private void ensurePlayers() {
		if (players == null) {
			players = new ArrayList<>();
		}
	}
}
