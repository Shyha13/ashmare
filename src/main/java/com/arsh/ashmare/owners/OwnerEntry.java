package com.arsh.ashmare.owners;

import java.util.Objects;
import java.util.UUID;

public record OwnerEntry(UUID uuid, String lastKnownUsername) {
	public OwnerEntry {
		lastKnownUsername = Objects.requireNonNull(
				lastKnownUsername,
				"lastKnownUsername"
		).trim();
		if (lastKnownUsername.isEmpty()) {
			throw new IllegalArgumentException("lastKnownUsername cannot be blank");
		}
	}

	public boolean isResolved() {
		return uuid != null;
	}

	public boolean matches(UUID playerUuid, String username) {
		Objects.requireNonNull(playerUuid, "playerUuid");
		if (uuid != null) {
			return uuid.equals(playerUuid);
		}
		return username != null
				&& lastKnownUsername.equalsIgnoreCase(username);
	}

	public boolean matches(String usernameOrUuid) {
		Objects.requireNonNull(usernameOrUuid, "usernameOrUuid");
		if (lastKnownUsername.equalsIgnoreCase(usernameOrUuid)) {
			return true;
		}
		if (uuid == null) {
			return false;
		}
		try {
			return uuid.equals(UUID.fromString(usernameOrUuid));
		} catch (IllegalArgumentException ignored) {
			return false;
		}
	}
}
