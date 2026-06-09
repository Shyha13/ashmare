package com.arsh.ashmare.names;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;
import java.util.Map;

public final class NameCommands {
	private NameCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("names")
				.then(Commands.literal("randomize")
						.executes(NameCommands::randomize))
				.then(Commands.literal("clear")
						.then(Commands.literal("all")
								.executes(NameCommands::clearAll))
						.then(Commands.argument(
										"player",
										GameProfileArgument.gameProfile()
								)
								.executes(NameCommands::clearPlayer))));
	}

	private static int randomize(CommandContext<CommandSourceStack> context) {
		Map<String, String> assignments = NameRandomizer.randomize(
				context.getSource().getServer()
		);

		if (assignments.isEmpty()) {
			context.getSource().sendFailure(
					Component.literal("No eligible online players to randomize.")
			);
			return 0;
		}

		context.getSource().sendSuccess(
				() -> Component.literal(
						"Randomized fake names for " + assignments.size() + " player(s)."
				),
				true
		);
		assignments.forEach((realName, fakeName) ->
				context.getSource().sendSuccess(
						() -> Component.literal("- " + realName + " -> " + fakeName),
						false
				)
		);
		return assignments.size();
	}

	private static int clearPlayer(
			CommandContext<CommandSourceStack> context
	) throws CommandSyntaxException {
		Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(
				context,
				"player"
		);

		int cleared = 0;
		for (NameAndId profile : profiles) {
			if (NameRandomizer.clear(context.getSource().getServer(), profile.id())) {
				cleared++;
			}
		}

		if (cleared == 0) {
			context.getSource().sendFailure(
					Component.literal("No fake-name assignment found.")
			);
			return 0;
		}

		int clearedCount = cleared;
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Cleared " + clearedCount + " fake-name assignment(s)."
				),
				true
		);
		return cleared;
	}

	private static int clearAll(CommandContext<CommandSourceStack> context) {
		int cleared = NameRandomizer.clearAll(context.getSource().getServer());
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Cleared " + cleared + " fake-name assignment(s)."
				),
				true
		);
		return cleared;
	}
}
