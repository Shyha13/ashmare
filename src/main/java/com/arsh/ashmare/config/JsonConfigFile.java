package com.arsh.ashmare.config;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonConfigFile<T> implements ConfigFile {
	private final Path path;
	private final Class<T> configType;
	private final Supplier<T> defaultFactory;
	private final Gson gson;
	private T value;

	public JsonConfigFile(Path path, Class<T> configType, Supplier<T> defaultFactory, Gson gson) {
		this.path = Objects.requireNonNull(path, "path");
		this.configType = Objects.requireNonNull(configType, "configType");
		this.defaultFactory = Objects.requireNonNull(defaultFactory, "defaultFactory");
		this.gson = Objects.requireNonNull(gson, "gson");
	}

	@Override
	public Path path() {
		return path;
	}

	@Override
	public synchronized void load() {
		if (Files.notExists(path)) {
			value = createDefault();
			save();
			return;
		}

		try {
			String json = Files.readString(path, StandardCharsets.UTF_8);
			T loadedValue = gson.fromJson(json, configType);

			if (loadedValue == null) {
				throw new JsonParseException("Config root cannot be null");
			}

			value = loadedValue;
		} catch (IOException | JsonParseException exception) {
			throw new ConfigLoadException(path, exception);
		}
	}

	@Override
	public synchronized void save() {
		ensureLoaded();
		write(value);
	}

	public synchronized T get() {
		ensureLoaded();
		return copy(value);
	}

	public synchronized <R> R read(Function<T, R> reader) {
		Objects.requireNonNull(reader, "reader");
		ensureLoaded();
		return reader.apply(value);
	}

	public synchronized void set(T newValue) {
		T checkedValue = Objects.requireNonNull(newValue, "newValue");
		write(checkedValue);
		value = copy(checkedValue);
	}

	public synchronized void update(Consumer<T> updater) {
		Objects.requireNonNull(updater, "updater");
		ensureLoaded();

		T updatedValue = copy(value);
		updater.accept(updatedValue);
		write(updatedValue);
		value = updatedValue;
	}

	public synchronized <R> R updateAndGet(Function<T, R> updater) {
		Objects.requireNonNull(updater, "updater");
		ensureLoaded();

		T updatedValue = copy(value);
		R result = updater.apply(updatedValue);
		write(updatedValue);
		value = updatedValue;
		return result;
	}

	private T createDefault() {
		return Objects.requireNonNull(defaultFactory.get(), "defaultFactory returned null");
	}

	private T copy(T source) {
		return gson.fromJson(gson.toJson(source), configType);
	}

	private void write(T source) {
		try {
			ConfigIo.writeString(path, gson.toJson(source) + System.lineSeparator());
		} catch (IOException exception) {
			throw new ConfigSaveException(path, exception);
		}
	}

	private void ensureLoaded() {
		if (value == null) {
			load();
		}
	}
}
