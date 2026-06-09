package com.arsh.ashmare.exclusions;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class ExclusionCommands {
	private ExclusionCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("exclude")
				.then(Commands.argument("player", EntityArgument.player())
						.executes(ExclusionCommands::exclude)));

		root.then(Commands.literal("include")
				.then(Commands.argument("player", EntityArgument.player())
						.executes(ExclusionCommands::include)));

		root.then(Commands.literal("excluded")
				.executes(ExclusionCommands::listExcluded));
	}

	private static int exclude(
			CommandContext<CommandSourceStack> context
	) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		boolean added = ExclusionManager.excludePlayer(player);
		String username = player.getGameProfile().name();

		context.getSource().sendSuccess(
				() -> Component.literal(added
						? "Excluded " + username + "."
						: username + " is already excluded; refreshed their username."),
				true
		);
		return added ? 1 : 0;
	}

	private static int include(
			CommandContext<CommandSourceStack> context
	) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		boolean removed = ExclusionManager.includePlayer(player.getUUID());
		String username = player.getGameProfile().name();

		if (removed) {
			context.getSource().sendSuccess(
					() -> Component.literal("Included " + username + "."),
					true
			);
			return 1;
		}

		context.getSource().sendFailure(Component.literal(username + " is not excluded."));
		return 0;
	}

	private static int listExcluded(CommandContext<CommandSourceStack> context) {
		List<ExcludedPlayer> excludedPlayers = ExclusionManager.excludedPlayers();

		if (excludedPlayers.isEmpty()) {
			context.getSource().sendSuccess(
					() -> Component.literal("No players are excluded."),
					false
			);
			return 0;
		}

		context.getSource().sendSuccess(
				() -> Component.literal("Excluded players (" + excludedPlayers.size() + "):"),
				false
		);

		for (ExcludedPlayer player : excludedPlayers) {
			context.getSource().sendSuccess(
					() -> Component.literal(
							"- " + player.lastKnownUsername() + " (" + player.uuid() + ")"
					),
					false
			);
		}

		return excludedPlayers.size();
	}
}
