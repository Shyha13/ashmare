package com.arsh.ashmare.skins;

import com.arsh.ashmare.config.LineListConfigFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SkinSourceDefaults {
	private static final List<String> USERNAMES = List.of(
			"Notch",
			"Dream",
			"Technoblade",
			"jeb_",
			"Dinnerbone",
			"CaptainSparklez",
			"Ph1LzA",
			"Skeppy",
			"BadBoyHalo",
			"GeorgeNotFound"
	);
	private static final List<String> FILE_CONTENTS = createFileContents();

	private SkinSourceDefaults() {
	}

	public static List<String> usernames() {
		return USERNAMES;
	}

	public static List<String> fileContents() {
		return FILE_CONTENTS;
	}

	public static boolean ensureUsableSources(LineListConfigFile file) {
		Objects.requireNonNull(file, "file");
		if (!SkinSourceParser.parse(file.get()).usernames().isEmpty()) {
			return false;
		}

		file.set(FILE_CONTENTS);
		return true;
	}

	private static List<String> createFileContents() {
		List<String> contents = new ArrayList<>();
		contents.add("# Ashmare skin source pool");
		contents.add("# Add real Minecraft Java usernames below, one per line.");
		contents.addAll(USERNAMES);
		return List.copyOf(contents);
	}
}
