package com.arsh.ashmare.skins;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class SkinSourceRequest {
	private SkinSourceRequest() {
	}

	static SkinSourceParser.Result select(
			List<String> configuredSources,
			Optional<String> requestedSource
	) {
		Objects.requireNonNull(configuredSources, "configuredSources");
		Objects.requireNonNull(requestedSource, "requestedSource");
		return SkinSourceParser.parse(
				requestedSource
						.<List<String>>map(List::of)
						.orElse(configuredSources)
		);
	}
}
