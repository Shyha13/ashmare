package com.arsh.ashmare.config;

import java.nio.file.Path;

public final class ConfigLoadException extends RuntimeException {
	public ConfigLoadException(Path path, Throwable cause) {
		super("Could not load Ashmare config file " + path, cause);
	}
}
