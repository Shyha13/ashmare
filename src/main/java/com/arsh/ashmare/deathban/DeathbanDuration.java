package com.arsh.ashmare.deathban;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public enum DeathbanDuration {
	THIRTY_MINUTES("30m", Duration.ofMinutes(30)),
	TWELVE_HOURS("12h", Duration.ofHours(12)),
	ONE_DAY("1d", Duration.ofDays(1)),
	PERMANENT("permanent", null);

	private final String configValue;
	private final Duration duration;

	DeathbanDuration(String configValue, Duration duration) {
		this.configValue = configValue;
		this.duration = duration;
	}

	public String configValue() {
		return configValue;
	}

	public Long expirationFrom(long startEpochMillis) {
		return duration == null ? null : startEpochMillis + duration.toMillis();
	}

	public static Optional<DeathbanDuration> parse(String value) {
		return Arrays.stream(values())
				.filter(duration -> duration.configValue.equalsIgnoreCase(value))
				.findFirst();
	}
}
