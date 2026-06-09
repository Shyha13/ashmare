package com.arsh.ashmare.deathban;

import java.util.Objects;
import java.util.UUID;

public record DeathbanEntry(
		UUID uuid,
		String lastKnownUsername,
		long diedAtEpochMillis,
		long banAtEpochMillis,
		String duration,
		Long expiresAtEpochMillis,
		DeathbanStatus status
) {
	public DeathbanEntry {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(lastKnownUsername, "lastKnownUsername");
		Objects.requireNonNull(duration, "duration");
		Objects.requireNonNull(status, "status");
	}

	public DeathbanEntry activate() {
		return new DeathbanEntry(
				uuid,
				lastKnownUsername,
				diedAtEpochMillis,
				banAtEpochMillis,
				duration,
				expiresAtEpochMillis,
				DeathbanStatus.ACTIVE
		);
	}

	public boolean isExpired(long nowEpochMillis) {
		return expiresAtEpochMillis != null && expiresAtEpochMillis <= nowEpochMillis;
	}
}
