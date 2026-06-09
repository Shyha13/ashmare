package com.arsh.ashmare.config;

import java.nio.file.Path;

public interface ConfigFile {
	Path path();

	void load();

	void save();
}
