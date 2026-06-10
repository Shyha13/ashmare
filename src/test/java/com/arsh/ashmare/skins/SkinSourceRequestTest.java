package com.arsh.ashmare.skins;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SkinSourceRequestTest {
	@Test
	void usesConfiguredPoolWhenNoSpecificSourceIsRequested() {
		SkinSourceParser.Result result = SkinSourceRequest.select(
				List.of("Notch", "Dream"),
				Optional.empty()
		);

		assertEquals(List.of("Notch", "Dream"), result.usernames());
		assertEquals(List.of(), result.invalidSources());
	}

	@Test
	void specificSourceDoesNotNeedToBeInConfiguredPool() {
		SkinSourceParser.Result result = SkinSourceRequest.select(
				List.of("Notch", "Dream"),
				Optional.of("Shyha")
		);

		assertEquals(List.of("Shyha"), result.usernames());
		assertEquals(List.of(), result.invalidSources());
	}

	@Test
	void rejectsAnInvalidSpecificSource() {
		SkinSourceParser.Result result = SkinSourceRequest.select(
				List.of("Notch"),
				Optional.of("not-a-java-name")
		);

		assertEquals(List.of(), result.usernames());
		assertEquals(
				List.of("not-a-java-name: invalid Minecraft Java username"),
				result.invalidSources()
		);
	}
}
