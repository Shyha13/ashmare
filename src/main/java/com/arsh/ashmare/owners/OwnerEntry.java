package com.arsh.ashmare.owners;

import java.util.Objects;
import java.util.UUID;

public record OwnerEntry(
		UUID uuid,
		String lastKnownUsername,
		boolean bypassNameRandomization,
		boolean bypassSkinRandomization
) {
	public OwnerEntry {
		lastKnownUsername = Objects.requireNonNull(
				lastKnownUsername,
				"lastKnownUsername"
		).trim();
		if (lastKnownUsername.isEmpty()) {
			throw new IllegalArgumentException("lastKnownUsername cannot be blank");
		}
	}

	public OwnerEntry(UUID uuid, String lastKnownUsername) {
		this(uuid, lastKnownUsername, false, false);
	}

	public boolean isResolved() {
		return uuid != null;
	}

	public boolean bypasses(OwnerBypass bypass) {
		return switch (Objects.requireNonNull(bypass, "bypass")) {
			case NAME -> bypassNameRandomization;
			case SKIN -> bypassSkinRandomization;
		};
	}

	public OwnerEntry withIdentity(UUID newUuid, String newUsername) {
		return new OwnerEntry(
				newUuid,
				newUsername,
				bypassNameRandomization,
				bypassSkinRandomization
		);
	}

	public OwnerEntry withBypass(OwnerBypass bypass, boolean enabled) {
		return switch (Objects.requireNonNull(bypass, "bypass")) {
			case NAME -> new OwnerEntry(
					uuid,
					lastKnownUsername,
					enabled,
					bypassSkinRandomization
			);
			case SKIN -> new OwnerEntry(
					uuid,
					lastKnownUsername,
					bypassNameRandomization,
					enabled
			);
		};
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
