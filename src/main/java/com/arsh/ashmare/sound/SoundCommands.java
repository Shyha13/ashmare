package com.arsh.ashmare.sound;

import com.arsh.ashmare.config.AshmareConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class SoundCommands {
	private SoundCommands() {
	}

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("sound")
				.then(Commands.literal("set")
						.then(Commands.argument("sound_id", IdentifierArgument.id())
								.executes(SoundCommands::setSound)))
				.then(Commands.literal("radius")
						.then(Commands.argument(
										"blocks",
										IntegerArgumentType.integer(0, 30_000_000)
								)
								.executes(SoundCommands::setRadius)))
				.then(Commands.literal("test")
						.executes(SoundCommands::testSound)));
	}

	private static int setSound(CommandContext<CommandSourceStack> context) {
		Identifier soundId = IdentifierArgument.getId(context, "sound_id");
		if (!DeathSoundManager.soundExists(soundId)) {
			context.getSource().sendFailure(
					Component.literal("Unknown sound: " + soundId)
			);
			return 0;
		}

		AshmareConfig.sound().update(config -> config.setSoundId(soundId.toString()));
		context.getSource().sendSuccess(
				() -> Component.literal("Death sound set to " + soundId + "."),
				true
		);
		return 1;
	}

	private static int setRadius(CommandContext<CommandSourceStack> context) {
		int radius = IntegerArgumentType.getInteger(context, "blocks");
		AshmareConfig.sound().update(config -> config.setRadius(radius));
		context.getSource().sendSuccess(
				() -> Component.literal("Death sound radius set to " + radius + " blocks."),
				true
		);
		return 1;
	}

	private static int testSound(CommandContext<CommandSourceStack> context) {
		Vec3 position = context.getSource().getPosition();
		int listeners = DeathSoundManager.play(
				context.getSource().getLevel(),
				position.x(),
				position.y(),
				position.z()
		);

		context.getSource().sendSuccess(
				() -> Component.literal(
						"Played the death sound for " + listeners + " player(s)."
				),
				false
		);
		return listeners;
	}
}
