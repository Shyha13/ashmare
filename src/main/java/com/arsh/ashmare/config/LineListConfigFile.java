package com.arsh.ashmare.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class LineListConfigFile implements ConfigFile {
	private final Path path;
	private final List<String> defaultValues;
	private List<String> values;

	public LineListConfigFile(Path path) {
		this(path, List.of());
	}

	public LineListConfigFile(Path path, List<String> defaultValues) {
		this.path = Objects.requireNonNull(path, "path");
		this.defaultValues = normalize(defaultValues);
	}

	@Override
	public Path path() {
		return path;
	}

	@Override
	public synchronized void load() {
		if (Files.notExists(path)) {
			values = List.copyOf(defaultValues);
			save();
			return;
		}

		try {
			values = normalize(Files.readAllLines(path, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new ConfigLoadException(path, exception);
		}
	}

	@Override
	public synchronized void save() {
		ensureLoaded();
		write(values);
	}

	public synchronized List<String> get() {
		ensureLoaded();
		return List.copyOf(values);
	}

	public synchronized void set(List<String> newValues) {
		List<String> normalizedValues = normalize(newValues);
		write(normalizedValues);
		values = normalizedValues;
	}

	public synchronized void update(Consumer<List<String>> updater) {
		Objects.requireNonNull(updater, "updater");
		ensureLoaded();

		List<String> updatedValues = new ArrayList<>(values);
		updater.accept(updatedValues);
		List<String> normalizedValues = normalize(updatedValues);
		write(normalizedValues);
		values = normalizedValues;
	}

	private List<String> normalize(List<String> source) {
		Objects.requireNonNull(source, "source");

		return source.stream()
				.map(value -> Objects.requireNonNull(value, "list value").trim())
				.filter(value -> !value.isEmpty())
				.toList();
	}

	private void write(List<String> source) {
		String content = source.isEmpty()
				? ""
				: String.join(System.lineSeparator(), source) + System.lineSeparator();

		try {
			ConfigIo.writeString(path, content);
		} catch (IOException exception) {
			throw new ConfigSaveException(path, exception);
		}
	}

	private void ensureLoaded() {
		if (values == null) {
			load();
		}
	}
}
