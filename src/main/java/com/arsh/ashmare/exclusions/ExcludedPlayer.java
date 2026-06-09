package com.arsh.ashmare.exclusions;

import java.util.Objects;
import java.util.UUID;

public record ExcludedPlayer(UUID uuid, String lastKnownUsername) {
	public ExcludedPlayer {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(lastKnownUsername, "lastKnownUsername");
	}
}
