package com.arsh.ashmare.skins;

import java.util.Objects;

public record SkinTextureProperty(
		String value,
		String signature
) {
	public SkinTextureProperty {
		Objects.requireNonNull(value, "value");
		Objects.requireNonNull(signature, "signature");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Skin texture value cannot be blank");
		}
		if (signature.isBlank()) {
			throw new IllegalArgumentException("Skin texture signature cannot be blank");
		}
	}
}
