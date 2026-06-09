package com.arsh.ashmare;

import com.arsh.ashmare.chat.ChatControl;
import com.arsh.ashmare.commands.AshmareCommands;
import com.arsh.ashmare.config.AshmareConfig;
import com.arsh.ashmare.deathban.DeathbanManager;
import com.arsh.ashmare.owners.OwnerManager;
import com.arsh.ashmare.presentation.PlayerProfilePresentation;
import com.arsh.ashmare.sound.DeathSoundManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AshmareMod implements ModInitializer {
	public static final String MOD_ID = "ashmare";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ChatControl.registerEvents();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				AshmareCommands.register(dispatcher)
		);

		ServerLifecycleEvents.SERVER_STARTING.register(server -> AshmareConfig.loadAll());
		ServerLifecycleEvents.SERVER_STARTED.register(DeathbanManager::start);
		ServerLifecycleEvents.SERVER_STARTED.register(OwnerManager::start);
		ServerLifecycleEvents.SERVER_STARTED.register(PlayerProfilePresentation::start);
		ServerLifecycleEvents.SERVER_STOPPING.register(DeathbanManager::stop);
		ServerLifecycleEvents.SERVER_STOPPING.register(PlayerProfilePresentation::stop);
		ServerLifecycleEvents.SERVER_STOPPING.register(OwnerManager::stop);
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) ->
						OwnerManager.observePlayer(server, handler.getPlayer())
		);

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayer player) {
				DeathSoundManager.onPlayerDeath(player);
				DeathbanManager.onPlayerDeath(player);
			}
		});

		LOGGER.info("Ashmare initialized.");
	}
}
