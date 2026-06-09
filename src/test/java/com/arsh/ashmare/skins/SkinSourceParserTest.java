package com.arsh.ashmare.skins;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SkinSourceParserTest {
	@Test
	void acceptsBomOnFirstUsername() {
		SkinSourceParser.Result result = SkinSourceParser.parse(
				List.of("\uFEFFNotch")
		);

		assertEquals(List.of("Notch"), result.usernames());
		assertEquals(List.of(), result.invalidSources());
	}

	@Test
	void acceptsCommentsAndCommonSeparators() {
		SkinSourceParser.Result result = SkinSourceParser.parse(List.of(
				"# one username per line",
				"Notch, jeb_; Shyha # server owner",
				"// another comment",
				"\"Dinnerbone\""
		));

		assertEquals(
				List.of("Notch", "jeb_", "Shyha", "Dinnerbone"),
				result.usernames()
		);
		assertEquals(List.of(), result.invalidSources());
	}

	@Test
	void deduplicatesAndReportsInvalidTokens() {
		SkinSourceParser.Result result = SkinSourceParser.parse(List.of(
				"Notch",
				"notch",
				"ab",
				"name-that-is-too-long"
		));

		assertEquals(List.of("Notch"), result.usernames());
		assertEquals(List.of(
				"ab: invalid Minecraft Java username",
				"name-that-is-too-long: invalid Minecraft Java username"
		), result.invalidSources());
	}
}
