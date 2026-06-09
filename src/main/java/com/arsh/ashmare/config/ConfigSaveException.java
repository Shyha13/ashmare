package com.arsh.ashmare.config;

import java.nio.file.Path;

public final class ConfigSaveException extends RuntimeException {
	public ConfigSaveException(Path path, Throwable cause) {
		super("Could not save Ashmare config file " + path, cause);
	}
}
