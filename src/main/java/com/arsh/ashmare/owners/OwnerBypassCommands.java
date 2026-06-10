package com.arsh.ashmare.owners;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class OwnerBypassCommands {
	private OwnerBypassCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("bypass")
				.executes(OwnerBypassCommands::list)
				.then(Commands.literal("name")
						.executes(context -> toggle(context, OwnerBypass.NAME)))
				.then(Commands.literal("skin")
						.executes(context -> toggle(context, OwnerBypass.SKIN)))
				.then(Commands.literal("list")
						.executes(OwnerBypassCommands::list)));
	}

	private static int toggle(
			CommandContext<CommandSourceStack> context,
			OwnerBypass bypass
	) {
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(
					Component.literal("Only configured owners can use this command.")
			);
			return 0;
		}

		Optional<OwnerEntry> updated = OwnerManager.toggleBypass(
				context.getSource().getServer(),
				player,
				bypass
		);
		if (updated.isEmpty()) {
			context.getSource().sendFailure(
					Component.literal("You are not a configured Ashmare owner.")
			);
			return 0;
		}

		boolean enabled = updated.get().bypasses(bypass);
		context.getSource().sendSuccess(
				() -> Component.literal(
						capitalize(bypass.commandName()) + " bypass is now "
								+ state(enabled) + "."
								+ (
										enabled
												? " Your real "
														+ bypass.commandName()
														+ " has been restored."
												: " You will be affected by the next "
														+ bypass.commandName()
														+ " randomization."
								)
				),
				false
		);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			return 0;
		}

		Optional<OwnerEntry> owner = OwnerManager.owner(player);
		if (owner.isEmpty()) {
			context.getSource().sendFailure(
					Component.literal("You are not a configured Ashmare owner.")
			);
			return 0;
		}

		OwnerEntry entry = owner.get();
		context.getSource().sendSuccess(
				() -> Component.literal("Owner bypasses:"),
				false
		);
		context.getSource().sendSuccess(
				() -> Component.literal(
						"- Name randomization: "
								+ state(entry.bypassNameRandomization())
				),
				false
		);
		context.getSource().sendSuccess(
				() -> Component.literal(
						"- Skin randomization: "
								+ state(entry.bypassSkinRandomization())
				),
				false
		);
		context.getSource().sendSuccess(
				() -> Component.literal(
						"- Deathban protection: ON (owner privilege)"
				),
				false
		);
		return 1;
	}

	private static String state(boolean enabled) {
		return enabled ? "ON" : "OFF";
	}

	private static String capitalize(String value) {
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
