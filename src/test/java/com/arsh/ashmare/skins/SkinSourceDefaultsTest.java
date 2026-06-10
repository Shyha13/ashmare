package com.arsh.ashmare.skins;

import com.arsh.ashmare.config.LineListConfigFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkinSourceDefaultsTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void replacesAnExistingEmptyFileWithTenDefaults() throws IOException {
		Path path = temporaryDirectory.resolve("skins.txt");
		Files.writeString(path, "");
		LineListConfigFile file = new LineListConfigFile(path);
		file.load();

		assertTrue(SkinSourceDefaults.ensureUsableSources(file));
		assertEquals(
				SkinSourceDefaults.usernames(),
				SkinSourceParser.parse(file.get()).usernames()
		);
		assertEquals(10, SkinSourceDefaults.usernames().size());
	}

	@Test
	void replacesAFileThatContainsNoUsableUsernames() throws IOException {
		Path path = temporaryDirectory.resolve("skins.txt");
		Files.write(path, List.of("# comment only", "not-a-username"));
		LineListConfigFile file = new LineListConfigFile(path);
		file.load();

		assertTrue(SkinSourceDefaults.ensureUsableSources(file));
		assertEquals(
				SkinSourceDefaults.usernames(),
				SkinSourceParser.parse(file.get()).usernames()
		);
	}

	@Test
	void preservesAUserConfiguredSourcePool() throws IOException {
		Path path = temporaryDirectory.resolve("skins.txt");
		Files.write(path, List.of("Shyha"));
		LineListConfigFile file = new LineListConfigFile(path);
		file.load();

		assertFalse(SkinSourceDefaults.ensureUsableSources(file));
		assertEquals(List.of("Shyha"), file.get());
	}
}
