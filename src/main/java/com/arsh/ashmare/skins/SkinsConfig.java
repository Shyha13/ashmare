package com.arsh.ashmare.skins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SkinsConfig {
	private List<SkinAssignment> assignments = new ArrayList<>();
	private List<CachedSkin> cache = new ArrayList<>();

	public List<SkinAssignment> assignments() {
		ensureLists();
		return List.copyOf(assignments);
	}

	public List<CachedSkin> cache() {
		ensureLists();
		return List.copyOf(cache);
	}

	public Optional<SkinAssignment> find(UUID playerUuid) {
		Objects.requireNonNull(playerUuid, "playerUuid");
		ensureLists();
		return assignments.stream()
				.filter(assignment -> assignment.playerUuid().equals(playerUuid))
				.findFirst();
	}

	public Optional<CachedSkin> findCached(String lookupUsername) {
		Objects.requireNonNull(lookupUsername, "lookupUsername");
		ensureLists();
		return cache.stream()
				.filter(entry -> entry.lookupUsername().equalsIgnoreCase(lookupUsername))
				.findFirst();
	}

	public void removeAll(Set<UUID> playerUuids) {
		Objects.requireNonNull(playerUuids, "playerUuids");
		ensureLists();
		assignments.removeIf(assignment ->
				playerUuids.contains(assignment.playerUuid())
		);
	}

	public boolean remove(UUID playerUuid) {
		Objects.requireNonNull(playerUuid, "playerUuid");
		ensureLists();
		return assignments.removeIf(assignment ->
				assignment.playerUuid().equals(playerUuid)
		);
	}

	public int clear() {
		ensureLists();
		int removed = assignments.size();
		assignments.clear();
		return removed;
	}

	public void put(SkinAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment");
		ensureLists();
		assignments.removeIf(existing ->
				existing.playerUuid().equals(assignment.playerUuid())
		);
		assignments.add(assignment);
		assignments.sort(Comparator.comparing(
				SkinAssignment::lastKnownUsername,
				String.CASE_INSENSITIVE_ORDER
		));
	}

	public void putCached(CachedSkin cachedSkin) {
		Objects.requireNonNull(cachedSkin, "cachedSkin");
		ensureLists();
		String cacheKey = cachedSkin.lookupUsername().toLowerCase(Locale.ROOT);
		cache.removeIf(existing ->
				existing.lookupUsername().toLowerCase(Locale.ROOT).equals(cacheKey)
		);
		cache.add(cachedSkin);
		cache.sort(Comparator.comparing(
				CachedSkin::lookupUsername,
				String.CASE_INSENSITIVE_ORDER
		));
	}

	private void ensureLists() {
		if (assignments == null) {
			assignments = new ArrayList<>();
		}
		if (cache == null) {
			cache = new ArrayList<>();
		}
	}
}
