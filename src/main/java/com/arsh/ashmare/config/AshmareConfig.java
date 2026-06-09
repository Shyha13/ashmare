package com.arsh.ashmare.config;

import com.arsh.ashmare.AshmareMod;
import com.arsh.ashmare.chat.ChatConfig;
import com.arsh.ashmare.deathban.DeathbanConfig;
import com.arsh.ashmare.exclusions.ExclusionsConfig;
import com.arsh.ashmare.names.NamesConfig;
import com.arsh.ashmare.owners.OwnersConfig;
import com.arsh.ashmare.skins.SkinsConfig;
import com.arsh.ashmare.sound.SoundConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public final class AshmareConfig {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();
	private static final Gson NULLABLE_GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeNulls()
			.create();

	private static final Path DIRECTORY = FabricLoader.getInstance()
			.getConfigDir()
			.resolve(AshmareMod.MOD_ID);

	private static final JsonConfigFile<DeathbanConfig> DEATHBAN =
			jsonFile("deathban.json", DeathbanConfig.class, DeathbanConfig::new);
	private static final JsonConfigFile<SoundConfig> SOUND =
			jsonFile("sound.json", SoundConfig.class, SoundConfig::new);
	private static final JsonConfigFile<ChatConfig> CHAT =
			jsonFile("chat.json", ChatConfig.class, ChatConfig::new);
	private static final JsonConfigFile<NamesConfig> NAMES =
			jsonFile("names.json", NamesConfig.class, NamesConfig::new);
	private static final JsonConfigFile<SkinsConfig> SKINS =
			jsonFile("skins.json", SkinsConfig.class, SkinsConfig::new);
	private static final JsonConfigFile<ExclusionsConfig> EXCLUSIONS =
			jsonFile("exclusions.json", ExclusionsConfig.class, ExclusionsConfig::new);
	private static final JsonConfigFile<OwnersConfig> OWNERS =
			new JsonConfigFile<>(
					DIRECTORY.resolve("owners.json"),
					OwnersConfig.class,
					OwnersConfig::firstStartupDefault,
					NULLABLE_GSON
			);
	private static final LineListConfigFile SKIN_USERNAMES =
			new LineListConfigFile(
					DIRECTORY.resolve("skins.txt"),
					List.of(
							"# Ashmare skin source pool",
							"# Add real Minecraft Java usernames below, one per line.",
							"Notch",
							"Dream",
							"Technoblade"
					)
			);

	private static final List<ConfigFile> FILES = List.of(
			DEATHBAN,
			SOUND,
			CHAT,
			NAMES,
			SKINS,
			EXCLUSIONS,
			OWNERS,
			SKIN_USERNAMES
	);

	private AshmareConfig() {
	}

	public static synchronized void loadAll() {
		createConfigDirectory();
		FILES.forEach(ConfigFile::load);
		CHAT.save();
		NAMES.save();
		SKINS.save();
		AshmareMod.LOGGER.info("Loaded {} Ashmare config files from {}.", FILES.size(), DIRECTORY);
	}

	public static Path directory() {
		return DIRECTORY;
	}

	public static JsonConfigFile<DeathbanConfig> deathban() {
		return DEATHBAN;
	}

	public static JsonConfigFile<SoundConfig> sound() {
		return SOUND;
	}

	public static JsonConfigFile<ChatConfig> chat() {
		return CHAT;
	}

	public static JsonConfigFile<NamesConfig> names() {
		return NAMES;
	}

	public static JsonConfigFile<SkinsConfig> skins() {
		return SKINS;
	}

	public static JsonConfigFile<ExclusionsConfig> exclusions() {
		return EXCLUSIONS;
	}

	public static JsonConfigFile<OwnersConfig> owners() {
		return OWNERS;
	}

	public static LineListConfigFile skinUsernames() {
		return SKIN_USERNAMES;
	}

	private static <T> JsonConfigFile<T> jsonFile(
			String fileName,
			Class<T> configType,
			Supplier<T> defaultFactory
	) {
		return new JsonConfigFile<>(
				DIRECTORY.resolve(fileName),
				configType,
				defaultFactory,
				GSON
		);
	}

	private static void createConfigDirectory() {
		try {
			Files.createDirectories(DIRECTORY);
		} catch (IOException exception) {
			throw new ConfigSaveException(DIRECTORY, exception);
		}
	}
}
