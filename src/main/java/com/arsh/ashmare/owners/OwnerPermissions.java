package com.arsh.ashmare.owners;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public final class OwnerPermissions {
	private static final Predicate<CommandSourceStack> GAMEMASTER =
			Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
	private static final Predicate<CommandSourceStack> SERVER_OWNER =
			Commands.hasPermission(Commands.LEVEL_OWNERS);

	private OwnerPermissions() {
	}

	public static boolean canUseAshmare(CommandSourceStack source) {
		return GAMEMASTER.test(source) || isConfiguredOwner(source);
	}

	public static boolean canManageOwners(CommandSourceStack source) {
		return SERVER_OWNER.test(source) || isConfiguredOwner(source);
	}

	public static boolean isConfiguredOwner(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		return player != null && OwnerManager.isOwner(player);
	}
}
