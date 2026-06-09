package com.arsh.ashmare.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class ConfigIo {
	private ConfigIo() {
	}

	static void writeString(Path path, String content) throws IOException {
		Files.createDirectories(path.getParent());

		Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
		Files.writeString(temporaryPath, content, StandardCharsets.UTF_8);

		try {
			Files.move(
					temporaryPath,
					path,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING
			);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
