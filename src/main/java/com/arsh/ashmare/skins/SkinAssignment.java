package com.arsh.ashmare.skins;

import java.util.Objects;
import java.util.UUID;

public record SkinAssignment(
		UUID playerUuid,
		String lastKnownUsername,
		UUID sourceUuid,
		String sourceUsername,
		SkinTextureProperty texture
) {
	public SkinAssignment {
		Objects.requireNonNull(playerUuid, "playerUuid");
		Objects.requireNonNull(lastKnownUsername, "lastKnownUsername");
		Objects.requireNonNull(sourceUuid, "sourceUuid");
		Objects.requireNonNull(sourceUsername, "sourceUsername");
		Objects.requireNonNull(texture, "texture");
		if (lastKnownUsername.isBlank()) {
			throw new IllegalArgumentException("Last known username cannot be blank");
		}
		if (sourceUsername.isBlank()) {
			throw new IllegalArgumentException("Source username cannot be blank");
		}
	}
}
