package com.arsh.ashmare.sound;

import com.arsh.ashmare.AshmareMod;
import com.arsh.ashmare.config.AshmareConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class DeathSoundManager {
	private DeathSoundManager() {
	}

	public static void onPlayerDeath(ServerPlayer player) {
		play(
				player.level(),
				player.getX(),
				player.getY(),
				player.getZ()
		);
	}

	public static int play(ServerLevel level, double x, double y, double z) {
		SoundConfig config = AshmareConfig.sound().get();
		Holder.Reference<SoundEvent> sound = resolveSound(config.soundId());

		double radiusSquared = (double) config.radius() * config.radius();
		float volume = Math.max(1.0F, config.radius() / 16.0F);
		ClientboundSoundPacket packet = new ClientboundSoundPacket(
				sound,
				SoundSource.WEATHER,
				x,
				y,
				z,
				volume,
				1.0F,
				ThreadLocalRandom.current().nextLong()
		);

		int listeners = 0;
		for (ServerPlayer listener : level.players()) {
			if (listener.distanceToSqr(x, y, z) <= radiusSquared) {
				listener.connection.send(packet);
				listeners++;
			}
		}
		return listeners;
	}

	public static boolean soundExists(Identifier soundId) {
		return BuiltInRegistries.SOUND_EVENT.containsKey(soundId);
	}

	private static Holder.Reference<SoundEvent> resolveSound(String configuredId) {
		Identifier soundId = Identifier.tryParse(configuredId);
		Optional<Holder.Reference<SoundEvent>> configuredSound = soundId == null
				? Optional.empty()
				: BuiltInRegistries.SOUND_EVENT.get(soundId);
		if (configuredSound.isPresent()) {
			return configuredSound.get();
		}

		AshmareMod.LOGGER.error(
				"Invalid Ashmare death sound {}; using {}.",
				configuredId,
				SoundConfig.DEFAULT_SOUND_ID
		);
		Identifier defaultId = Identifier.parse(SoundConfig.DEFAULT_SOUND_ID);
		return BuiltInRegistries.SOUND_EVENT.get(defaultId)
				.orElseThrow(() -> new IllegalStateException(
						"Missing vanilla death sound " + defaultId
				));
	}
}
