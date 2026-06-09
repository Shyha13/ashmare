package com.arsh.ashmare.skins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

final class SkinSourceParser {
	private static final char BYTE_ORDER_MARK = '\uFEFF';
	private static final Pattern USERNAME_PATTERN =
			Pattern.compile("[A-Za-z0-9_]{3,16}");
	private static final Pattern SEPARATOR_PATTERN =
			Pattern.compile("[\\s,;]+");

	private SkinSourceParser() {
	}

	static Result parse(List<String> sourceLines) {
		Objects.requireNonNull(sourceLines, "sourceLines");

		Map<String, String> unique = new LinkedHashMap<>();
		List<String> invalid = new ArrayList<>();
		for (String sourceLine : sourceLines) {
			String line = removeByteOrderMark(
					Objects.requireNonNull(sourceLine, "sourceLine")
			).strip();
			if (line.isEmpty() || line.startsWith("#")
					|| line.startsWith("//")) {
				continue;
			}

			int commentStart = line.indexOf('#');
			if (commentStart >= 0) {
				line = line.substring(0, commentStart).strip();
			}
			if (line.isEmpty()) {
				continue;
			}

			for (String token : SEPARATOR_PATTERN.split(line)) {
				String username = stripWrappingPunctuation(token);
				if (username.isEmpty()) {
					continue;
				}
				if (!USERNAME_PATTERN.matcher(username).matches()) {
					invalid.add(username + ": invalid Minecraft Java username");
					continue;
				}
				unique.putIfAbsent(
						username.toLowerCase(Locale.ROOT),
						username
				);
			}
		}

		return new Result(
				List.copyOf(unique.values()),
				List.copyOf(invalid)
		);
	}

	private static String removeByteOrderMark(String value) {
		if (!value.isEmpty() && value.charAt(0) == BYTE_ORDER_MARK) {
			return value.substring(1);
		}
		return value;
	}

	private static String stripWrappingPunctuation(String value) {
		int start = 0;
		int end = value.length();
		while (start < end && isWrapper(value.charAt(start))) {
			start++;
		}
		while (end > start && isWrapper(value.charAt(end - 1))) {
			end--;
		}
		return value.substring(start, end);
	}

	private static boolean isWrapper(char character) {
		return character == '"' || character == '\''
				|| character == '`' || character == '['
				|| character == ']' || character == '('
				|| character == ')';
	}

	record Result(List<String> usernames, List<String> invalidSources) {
		Result {
			usernames = List.copyOf(usernames);
			invalidSources = List.copyOf(invalidSources);
		}
	}
}
