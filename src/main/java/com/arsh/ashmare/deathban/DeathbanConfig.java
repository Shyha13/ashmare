package com.arsh.ashmare.deathban;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class DeathbanConfig {
	private int delaySeconds = 10;
	private String duration = DeathbanDuration.ONE_DAY.configValue();
	private List<DeathbanEntry> activeDeathbans = new ArrayList<>();

	public int delaySeconds() {
		return Math.max(0, delaySeconds);
	}

	public void setDelaySeconds(int delaySeconds) {
		if (delaySeconds < 0) {
			throw new IllegalArgumentException("Deathban delay cannot be negative");
		}

		this.delaySeconds = delaySeconds;
	}

	public DeathbanDuration duration() {
		return DeathbanDuration.parse(duration).orElse(DeathbanDuration.ONE_DAY);
	}

	public void setDuration(DeathbanDuration duration) {
		this.duration = Objects.requireNonNull(duration, "duration").configValue();
	}

	public List<DeathbanEntry> activeDeathbans() {
		ensureEntries();
		return List.copyOf(activeDeathbans);
	}

	public Optional<DeathbanEntry> find(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensureEntries();
		return activeDeathbans.stream()
				.filter(entry -> entry.uuid().equals(uuid))
				.findFirst();
	}

	public Optional<DeathbanEntry> find(String usernameOrUuid) {
		Objects.requireNonNull(usernameOrUuid, "usernameOrUuid");
		ensureEntries();

		return activeDeathbans.stream()
				.filter(entry ->
						entry.lastKnownUsername().equalsIgnoreCase(usernameOrUuid)
								|| entry.uuid().toString().equalsIgnoreCase(usernameOrUuid)
				)
				.findFirst();
	}

	public void upsert(DeathbanEntry entry) {
		Objects.requireNonNull(entry, "entry");
		ensureEntries();
		activeDeathbans.removeIf(existing -> existing.uuid().equals(entry.uuid()));
		activeDeathbans.add(entry);
		activeDeathbans.sort(Comparator.comparing(
				DeathbanEntry::lastKnownUsername,
				String.CASE_INSENSITIVE_ORDER
		));
	}

	public boolean remove(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensureEntries();
		return activeDeathbans.removeIf(entry -> entry.uuid().equals(uuid));
	}

	private void ensureEntries() {
		if (activeDeathbans == null) {
			activeDeathbans = new ArrayList<>();
		}
	}
}
