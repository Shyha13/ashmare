package com.arsh.ashmare.names;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class FakeNameGenerator {
	private static final int MIN_LENGTH = 6;
	private static final int MAX_LENGTH = 14;
	private static final int MAX_ATTEMPTS = 10_000;

	private static final List<String> FRAGMENTS = List.of(
			"star",
			"watch",
			"dingo",
			"bex",
			"cargo",
			"band",
			"len",
			"mxod",
			"luna",
			"nova",
			"kel",
			"ven",
			"ry",
			"fox",
			"echo",
			"zed",
			"kai"
	);

	private static final List<String> SUFFIXES = List.of("MC", "XD", "TV");

	private FakeNameGenerator() {
	}

	public static String generateUnique(Set<String> usedNames) {
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			String candidate = generate(random);
			if (!usedNames.contains(candidate.toLowerCase(Locale.ROOT))) {
				return candidate;
			}
		}

		throw new IllegalStateException("Could not generate a unique fake name");
	}

	private static String generate(ThreadLocalRandom random) {
		int fragmentCount = random.nextInt(100) < 60 ? 2 : 3;
		List<String> fragments = pickFragments(random, fragmentCount);
		int underscoreAfter = random.nextInt(100) < 20
				? random.nextInt(fragmentCount - 1)
				: -1;

		StringBuilder name = new StringBuilder();
		for (int index = 0; index < fragments.size(); index++) {
			name.append(fragments.get(index));
			if (index == underscoreAfter) {
				name.append('_');
			}
		}

		String candidate = substituteLetters(name.toString(), random);

		if (random.nextInt(100) < 25) {
			int digitCount = random.nextInt(1, 5);
			StringBuilder digits = new StringBuilder(digitCount);
			for (int index = 0; index < digitCount; index++) {
				digits.append(random.nextInt(10));
			}
			candidate += digits;
		}

		candidate = applyCasing(candidate, random);
		return candidate.length() >= MIN_LENGTH && candidate.length() <= MAX_LENGTH
				? candidate
				: generate(random);
	}

	private static List<String> pickFragments(
			ThreadLocalRandom random,
			int fragmentCount
	) {
		List<String> available = new ArrayList<>(FRAGMENTS);
		List<String> selected = new ArrayList<>(fragmentCount);

		for (int index = 0; index < fragmentCount; index++) {
			selected.add(available.remove(random.nextInt(available.size())));
		}
		return selected;
	}

	private static String substituteLetters(
			String value,
			ThreadLocalRandom random
	) {
		if (random.nextInt(100) >= 30) {
			return value;
		}

		StringBuilder result = new StringBuilder(value.length());
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			if (random.nextInt(100) < 25) {
				character = switch (character) {
					case 'o' -> '0';
					case 'a' -> '4';
					case 'e' -> '3';
					case 'i' -> '1';
					default -> character;
				};
			}
			result.append(character);
		}
		return result.toString();
	}

	private static String applyCasing(
			String value,
			ThreadLocalRandom random
	) {
		return switch (random.nextInt(4)) {
			case 0 -> value.toLowerCase(Locale.ROOT);
			case 1 -> value.toUpperCase(Locale.ROOT);
			case 2 -> sentenceCase(value);
			default -> mixedSuffix(value, random);
		};
	}

	private static String sentenceCase(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0))
				+ value.substring(1).toLowerCase(Locale.ROOT);
	}

	private static String mixedSuffix(
			String value,
			ThreadLocalRandom random
	) {
		String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
		String base = random.nextBoolean()
				? value.toLowerCase(Locale.ROOT)
				: sentenceCase(value);
		return base + suffix;
	}
}
