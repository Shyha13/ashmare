package com.arsh.ashmare.owners;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;
import java.util.List;

public final class OwnerCommands {
	private OwnerCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("owners")
				.requires(OwnerPermissions::canManageOwners)
				.then(Commands.literal("list")
						.executes(OwnerCommands::list))
				.then(Commands.literal("add")
						.then(Commands.argument(
										"player",
										GameProfileArgument.gameProfile()
								)
								.executes(OwnerCommands::add)))
				.then(Commands.literal("remove")
						.then(Commands.argument(
										"player",
										StringArgumentType.word()
								)
								.executes(OwnerCommands::remove))));
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		List<OwnerEntry> owners = OwnerManager.owners();
		if (owners.isEmpty()) {
			context.getSource().sendSuccess(
					() -> Component.literal("No Ashmare owners configured."),
					false
			);
			return 0;
		}

		context.getSource().sendSuccess(
				() -> Component.literal("Ashmare owners (" + owners.size() + "):"),
				false
		);
		for (OwnerEntry owner : owners) {
			context.getSource().sendSuccess(
					() -> Component.literal(
							"- " + owner.lastKnownUsername() + " ("
									+ (
											owner.uuid() == null
													? "UUID unresolved"
													: owner.uuid()
									)
									+ ")"
					),
					false
			);
		}
		return owners.size();
	}

	private static int add(
			CommandContext<CommandSourceStack> context
	) throws CommandSyntaxException {
		Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(
				context,
				"player"
		);
		int added = 0;
		for (NameAndId profile : profiles) {
			if (OwnerManager.addOwner(context.getSource().getServer(), profile)) {
				added++;
			}
		}

		int addedCount = added;
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Added " + addedCount + " Ashmare owner(s); "
								+ "existing owner identities were refreshed."
				),
				true
		);
		return added;
	}

	private static int remove(CommandContext<CommandSourceStack> context) {
		String player = StringArgumentType.getString(context, "player");
		if (!OwnerManager.removeOwner(player)) {
			context.getSource().sendFailure(
					Component.literal("No Ashmare owner found for " + player + ".")
			);
			return 0;
		}

		context.getSource().sendSuccess(
				() -> Component.literal("Removed Ashmare owner " + player + "."),
				true
		);
		return 1;
	}
}
