package com.arsh.ashmare.skins;

import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.exclusions.ExclusionManager;
import com.arsh.ashmare.owners.OwnerManager;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SkinPresentation {
	private static final String TEXTURES_PROPERTY = "textures";

	private SkinPresentation() {
	}

	public static Optional<SkinAssignment> assignment(UUID playerUuid) {
		Objects.requireNonNull(playerUuid, "playerUuid");
		if (
			ExclusionManager.isExcluded(playerUuid)
					|| OwnerManager.isOwner(playerUuid)
		) {
			return Optional.empty();
		}
		return AshmareConfig.skins().read(config -> config.find(playerUuid));
	}

	public static Optional<SkinAssignment> assignment(
			UUID playerUuid,
			String username
	) {
		Objects.requireNonNull(playerUuid, "playerUuid");
		if (
			ExclusionManager.isExcluded(playerUuid)
					|| OwnerManager.isOwner(playerUuid, username)
		) {
			return Optional.empty();
		}
		return AshmareConfig.skins().read(config -> config.find(playerUuid));
	}

	public static PropertyMap apply(
			PropertyMap originalProperties,
			SkinAssignment assignment
	) {
		Objects.requireNonNull(originalProperties, "originalProperties");
		Objects.requireNonNull(assignment, "assignment");

		ImmutableMultimap.Builder<String, Property> properties =
				ImmutableMultimap.builder();
		for (Map.Entry<String, Property> entry :
				originalProperties.entries()) {
			if (!entry.getKey().equals(TEXTURES_PROPERTY)) {
				properties.put(entry);
			}
		}

		SkinTextureProperty texture = assignment.texture();
		properties.put(
				TEXTURES_PROPERTY,
				new Property(
						TEXTURES_PROPERTY,
						texture.value(),
						texture.signature()
				)
		);
		return new PropertyMap(properties.build());
	}
}
