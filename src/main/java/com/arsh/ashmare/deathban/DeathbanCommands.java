package com.arsh.ashmare.deathban;

import com.arsh.ashmare.config.AshmareConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class DeathbanCommands {
	private DeathbanCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("ban")
				.then(Commands.literal("delay")
						.then(Commands.argument("seconds", IntegerArgumentType.integer(0))
								.executes(DeathbanCommands::setDelay)))
				.then(Commands.literal("duration")
						.then(Commands.argument("time", StringArgumentType.word())
								.executes(DeathbanCommands::setDuration)))
				.then(Commands.literal("list")
						.executes(DeathbanCommands::list)));

		root.then(Commands.literal("unban")
				.then(Commands.argument("player", StringArgumentType.word())
						.executes(DeathbanCommands::unban)));
	}

	private static int setDelay(CommandContext<CommandSourceStack> context) {
		int seconds = IntegerArgumentType.getInteger(context, "seconds");
		AshmareConfig.deathban().update(config -> config.setDelaySeconds(seconds));
		context.getSource().sendSuccess(
				() -> Component.literal("Deathban delay set to " + seconds + " seconds."),
				true
		);
		return 1;
	}

	private static int setDuration(CommandContext<CommandSourceStack> context) {
		String value = StringArgumentType.getString(context, "time");
		Optional<DeathbanDuration> duration = DeathbanDuration.parse(value);

		if (duration.isEmpty()) {
			context.getSource().sendFailure(
					Component.literal("Invalid duration. Use 30m, 12h, 1d, or permanent.")
			);
			return 0;
		}

		AshmareConfig.deathban().update(config -> config.setDuration(duration.get()));
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Deathban duration set to " + duration.get().configValue() + "."
				),
				true
		);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		List<DeathbanEntry> entries = DeathbanManager.entries();
		if (entries.isEmpty()) {
			context.getSource().sendSuccess(
					() -> Component.literal("No pending or active deathbans."),
					false
			);
			return 0;
		}

		context.getSource().sendSuccess(
				() -> Component.literal("Deathbans (" + entries.size() + "):"),
				false
		);

		for (DeathbanEntry entry : entries) {
			context.getSource().sendSuccess(
					() -> Component.literal(formatEntry(entry)),
					false
			);
		}
		return entries.size();
	}

	private static int unban(CommandContext<CommandSourceStack> context) {
		String player = StringArgumentType.getString(context, "player");
		boolean removed = DeathbanManager.unban(context.getSource().getServer(), player);

		if (!removed) {
			context.getSource().sendFailure(
					Component.literal("No Ashmare deathban found for " + player + ".")
			);
			return 0;
		}

		context.getSource().sendSuccess(
				() -> Component.literal("Removed the deathban for " + player + "."),
				true
		);
		return 1;
	}

	private static String formatEntry(DeathbanEntry entry) {
		String state;
		if (entry.status() == DeathbanStatus.PENDING) {
			state = "pending until " + Instant.ofEpochMilli(entry.banAtEpochMillis());
		} else if (entry.expiresAtEpochMillis() == null) {
			state = "active permanently";
		} else {
			state = "active until " + Instant.ofEpochMilli(entry.expiresAtEpochMillis());
		}

		return "- " + entry.lastKnownUsername() + " (" + entry.uuid() + "): " + state;
	}
}
