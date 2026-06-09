package com.arsh.ashmare.skins;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SkinRandomizationResult(
		Map<String, String> assignments,
		int resolvedSources,
		List<String> failedSources,
		List<String> warnings,
		String failureMessage
) {
	public SkinRandomizationResult {
		assignments = Map.copyOf(Objects.requireNonNull(assignments, "assignments"));
		failedSources = List.copyOf(
				Objects.requireNonNull(failedSources, "failedSources")
		);
		warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
	}

	public boolean successful() {
		return failureMessage == null;
	}

	public static SkinRandomizationResult failure(
			String message,
			List<String> failedSources
	) {
		return new SkinRandomizationResult(
				Map.of(),
				0,
				failedSources,
				List.of(),
				Objects.requireNonNull(message, "message")
		);
	}
}
