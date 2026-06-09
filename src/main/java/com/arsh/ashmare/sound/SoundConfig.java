package com.arsh.ashmare.sound;

import java.util.Objects;

public final class SoundConfig {
	public static final String DEFAULT_SOUND_ID =
			"minecraft:entity.lightning_bolt.thunder";
	public static final int DEFAULT_RADIUS = 64;

	private String soundId = DEFAULT_SOUND_ID;
	private int radius = DEFAULT_RADIUS;

	public String soundId() {
		return soundId == null || soundId.isBlank() ? DEFAULT_SOUND_ID : soundId;
	}

	public void setSoundId(String soundId) {
		this.soundId = Objects.requireNonNull(soundId, "soundId");
	}

	public int radius() {
		return Math.max(0, radius);
	}

	public void setRadius(int radius) {
		if (radius < 0) {
			throw new IllegalArgumentException("Sound radius cannot be negative");
		}

		this.radius = radius;
	}
}
