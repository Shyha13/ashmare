package com.arsh.ashmare.chat;

import com.arsh.ashmare.config.AshmareConfig;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class ChatControl {
	public static final String DEATH_MESSAGE_LABEL =
			"required for the deathban system";

	private static final String ADVANCEMENT_PREFIX = "chat.type.advancement.";
	private static final String JOIN_PREFIX = "multiplayer.player.joined";
	private static final String LEAVE_KEY = "multiplayer.player.left";
	private static final String DEATH_PREFIX = "death.";

	private ChatControl() {
	}

	public static void registerEvents() {
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
				(message, sender, params) ->
						AshmareConfig.chat().get().playerChatMessages()
		);

		ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register(
				(message, source, params) ->
						AshmareConfig.chat().get().commandOutputToOthers()
		);

		ServerMessageEvents.ALLOW_GAME_MESSAGE.register(
				(server, message, overlay) -> shouldAllowGameMessage(message)
		);
	}

	public static boolean commandOutputToOthersEnabled() {
		return AshmareConfig.chat().get().commandOutputToOthers();
	}

	public static boolean shouldSuppressTeamDeathMessage(Component message) {
		return !AshmareConfig.chat().get().deathMessages() && isDeathMessage(message);
	}

	private static boolean shouldAllowGameMessage(Component message) {
		ChatConfig config = AshmareConfig.chat().get();
		String translationKey = translationKey(message);
		if (translationKey == null) {
			return true;
		}

		if (translationKey.startsWith(ADVANCEMENT_PREFIX)) {
			return config.advancementAnnouncements();
		}
		if (translationKey.startsWith(JOIN_PREFIX) || translationKey.equals(LEAVE_KEY)) {
			return config.joinLeaveMessages();
		}
		if (translationKey.startsWith(DEATH_PREFIX)) {
			return config.deathMessages();
		}
		return true;
	}

	private static boolean isDeathMessage(Component message) {
		String translationKey = translationKey(message);
		return translationKey != null && translationKey.startsWith(DEATH_PREFIX);
	}

	private static String translationKey(Component message) {
		if (message.getContents() instanceof TranslatableContents translatable) {
			return translatable.getKey();
		}
		return null;
	}
}
