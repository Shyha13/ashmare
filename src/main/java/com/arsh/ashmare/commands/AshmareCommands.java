package com.arsh.ashmare.commands;

import com.arsh.ashmare.chat.ChatCommands;
import com.arsh.ashmare.deathban.DeathbanCommands;
import com.arsh.ashmare.exclusions.ExclusionCommands;
import com.arsh.ashmare.names.NameCommands;
import com.arsh.ashmare.owners.OwnerBypassCommands;
import com.arsh.ashmare.owners.OwnerCommands;
import com.arsh.ashmare.owners.OwnerPermissions;
import com.arsh.ashmare.skins.SkinCommands;
import com.arsh.ashmare.sound.SoundCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class AshmareCommands {
	private AshmareCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ashmare")
				.requires(OwnerPermissions::canUseAshmare)
				.executes(AshmareCommands::sendHelp)
				.then(Commands.literal("help")
						.executes(AshmareCommands::sendHelp));

		ExclusionCommands.register(root);
		DeathbanCommands.register(root);
		SoundCommands.register(root);
		ChatCommands.register(root);
		NameCommands.register(root);
		SkinCommands.register(root);
		OwnerCommands.register(root);
		OwnerBypassCommands.register(root);
		dispatcher.register(root);
	}

	private static int sendHelp(CommandContext<CommandSourceStack> context) {
		context.getSource().sendSuccess(
				() -> Component.literal(
						"Ashmare commands: /ashmare help, ban, unban, sound, chat, names, "
								+ "skins, exclude, include, excluded, owners, bypass"
				),
				false
		);
		return 1;
	}
}
