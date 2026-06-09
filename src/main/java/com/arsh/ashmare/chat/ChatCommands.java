package com.arsh.ashmare.chat;

import com.arsh.ashmare.config.AshmareConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public final class ChatCommands {
	private ChatCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		LiteralArgumentBuilder<CommandSourceStack> chat = Commands.literal("chat")
				.executes(ChatCommands::openMenu);

		chat.then(toggle(
				"advancements",
				ChatCommands::setAdvancements
		));
		chat.then(toggle(
				"joinleave",
				ChatCommands::setJoinLeave
		));
		chat.then(toggle(
				"playerchat",
				ChatCommands::setPlayerChat
		));
		chat.then(toggle(
				"commandoutput",
				ChatCommands::setCommandOutput
		));
		chat.then(toggle(
				"deaths",
				ChatCommands::setDeathMessages
		));

		root.then(chat);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> toggle(
			String name,
			com.mojang.brigadier.Command<CommandSourceStack> command
	) {
		return Commands.literal(name)
				.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(command));
	}

	private static int setAdvancements(CommandContext<CommandSourceStack> context) {
		boolean enabled = enabled(context);
		AshmareConfig.chat().update(
				config -> config.setAdvancementAnnouncements(enabled)
		);
		return confirm(context, "Advancement announcements", enabled, null);
	}

	private static int setJoinLeave(CommandContext<CommandSourceStack> context) {
		boolean enabled = enabled(context);
		AshmareConfig.chat().update(config -> config.setJoinLeaveMessages(enabled));
		return confirm(context, "Join/leave messages", enabled, null);
	}

	private static int setPlayerChat(CommandContext<CommandSourceStack> context) {
		boolean enabled = enabled(context);
		AshmareConfig.chat().update(config -> config.setPlayerChatMessages(enabled));
		return confirm(context, "Player chat messages", enabled, null);
	}

	private static int setCommandOutput(CommandContext<CommandSourceStack> context) {
		boolean enabled = enabled(context);
		AshmareConfig.chat().update(config -> config.setCommandOutputToOthers(enabled));
		return confirm(context, "Command output to other players", enabled, null);
	}

	private static int setDeathMessages(CommandContext<CommandSourceStack> context) {
		boolean enabled = enabled(context);
		AshmareConfig.chat().update(config -> config.setDeathMessages(enabled));
		return confirm(
				context,
				"Death messages",
				enabled,
				ChatControl.DEATH_MESSAGE_LABEL
		);
	}

	private static int openMenu(
			CommandContext<CommandSourceStack> context
	) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		if (!ChatControlMenu.open(player)) {
			context.getSource().sendFailure(
					Component.literal("Could not open the Ashmare chat controls.")
			);
			return 0;
		}
		return 1;
	}

	private static boolean enabled(CommandContext<CommandSourceStack> context) {
		return BoolArgumentType.getBool(context, "enabled");
	}

	private static int confirm(
			CommandContext<CommandSourceStack> context,
			String label,
			boolean enabled,
			String note
	) {
		context.getSource().sendSuccess(
				() -> Component.literal(format(label, enabled, note)),
				true
		);
		return 1;
	}

	private static String format(String label, boolean enabled, String note) {
		String value = label + ": " + (enabled ? "ON" : "OFF");
		return note == null ? value : value + " (" + note + ")";
	}
}
