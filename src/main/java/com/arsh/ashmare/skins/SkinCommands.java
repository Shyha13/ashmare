package com.arsh.ashmare.skins;

import com.arsh.ashmare.AshmareMod;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;
import java.util.concurrent.CompletionException;

public final class SkinCommands {
	private static final int MAX_REPORTED_ISSUES = 5;

	private SkinCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("skins")
				.then(Commands.literal("randomize")
						.executes(SkinCommands::randomize))
				.then(Commands.literal("clear")
						.then(Commands.literal("all")
								.executes(SkinCommands::clearAll))
						.then(Commands.argument(
										"player",
										GameProfileArgument.gameProfile()
								)
								.executes(SkinCommands::clearPlayer))));
	}

	private static int randomize(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		var future = SkinRandomizer.randomize(server);
		if (future.isEmpty()) {
			source.sendFailure(
					Component.literal("A skin randomization is already running.")
			);
			return 0;
		}

		source.sendSuccess(
				() -> Component.literal(
						"Resolving skin sources from config/ashmare/skins.txt..."
				),
				false
		);
		future.get().whenComplete((result, throwable) ->
				server.execute(() -> {
					if (throwable != null) {
						Throwable cause = unwrap(throwable);
						AshmareMod.LOGGER.error(
								"Skin randomization failed.",
								cause
						);
						source.sendFailure(Component.literal(
								"Skin randomization failed: " + safeMessage(cause)
						));
						return;
					}
					reportResult(source, result);
				})
		);
		return 1;
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
			if (SkinRandomizer.clear(
					context.getSource().getServer(),
					profile.id()
			)) {
				cleared++;
			}
		}

		if (cleared == 0) {
			context.getSource().sendFailure(
					Component.literal("No skin assignment found.")
			);
			return 0;
		}

		int clearedCount = cleared;
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Cleared " + clearedCount + " skin assignment(s)."
				),
				true
		);
		return cleared;
	}

	private static int clearAll(CommandContext<CommandSourceStack> context) {
		int cleared = SkinRandomizer.clearAll(
				context.getSource().getServer()
		);
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Cleared " + cleared + " skin assignment(s)."
				),
				true
		);
		return cleared;
	}

	private static void reportResult(
			CommandSourceStack source,
			SkinRandomizationResult result
	) {
		if (!result.successful()) {
			source.sendFailure(Component.literal(result.failureMessage()));
			reportIssues(source, result.failedSources(), "Skipped source");
			return;
		}

		source.sendSuccess(
				() -> Component.literal(
						"Assigned randomized skins to "
								+ result.assignments().size()
								+ " player(s) using "
								+ result.resolvedSources()
								+ " resolved source skin(s)."
				),
				true
		);
		result.assignments().forEach((player, skinSource) ->
				source.sendSuccess(
						() -> Component.literal(
								"- " + player + " <- " + skinSource
						),
						false
				)
		);
		reportIssues(source, result.failedSources(), "Skipped source");
		reportIssues(source, result.warnings(), "Cache warning");
	}

	private static void reportIssues(
			CommandSourceStack source,
			java.util.List<String> issues,
			String label
	) {
		issues.forEach(issue ->
				AshmareMod.LOGGER.warn("{}: {}", label, issue)
		);
		issues.stream()
				.limit(MAX_REPORTED_ISSUES)
				.forEach(issue -> source.sendFailure(
						Component.literal(label + ": " + issue)
				));
		if (issues.size() > MAX_REPORTED_ISSUES) {
			source.sendFailure(Component.literal(
					label + ": " + (issues.size() - MAX_REPORTED_ISSUES)
							+ " more issue(s); see the server log."
			));
		}
	}

	private static Throwable unwrap(Throwable throwable) {
		if (throwable instanceof CompletionException
				&& throwable.getCause() != null) {
			return throwable.getCause();
		}
		return throwable;
	}

	private static String safeMessage(Throwable throwable) {
		String message = throwable.getMessage();
		return message == null || message.isBlank()
				? throwable.getClass().getSimpleName()
				: message;
	}
}
