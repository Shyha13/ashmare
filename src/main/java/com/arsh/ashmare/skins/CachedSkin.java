package com.arsh.ashmare.skins;

import java.util.Objects;
import java.util.UUID;

public record CachedSkin(
		String lookupUsername,
		UUID sourceUuid,
		String sourceUsername,
		SkinTextureProperty texture,
		long fetchedAtEpochMillis
) {
	public CachedSkin {
		Objects.requireNonNull(lookupUsername, "lookupUsername");
		Objects.requireNonNull(sourceUuid, "sourceUuid");
		Objects.requireNonNull(sourceUsername, "sourceUsername");
		Objects.requireNonNull(texture, "texture");
		if (lookupUsername.isBlank()) {
			throw new IllegalArgumentException("Lookup username cannot be blank");
		}
		if (sourceUsername.isBlank()) {
			throw new IllegalArgumentException("Source username cannot be blank");
		}
	}
}
