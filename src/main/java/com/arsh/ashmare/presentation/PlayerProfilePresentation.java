package com.arsh.ashmare.presentation;

import com.arsh.ashmare.mixin.ChunkMapAccessor;
import com.arsh.ashmare.mixin.TrackedEntityAccessor;
import com.arsh.ashmare.names.NamePresentation;
import com.arsh.ashmare.skins.SkinAssignment;
import com.arsh.ashmare.skins.SkinPresentation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodData;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerProfilePresentation {
	private static MinecraftServer server;

	private PlayerProfilePresentation() {
	}

	public static void start(MinecraftServer minecraftServer) {
		server = Objects.requireNonNull(minecraftServer, "minecraftServer");
	}

	public static void stop(MinecraftServer minecraftServer) {
		if (server == minecraftServer) {
			server = null;
		}
	}

	public static ClientboundPlayerInfoUpdatePacket.Entry rewriteEntry(
			ClientboundPlayerInfoUpdatePacket.Entry entry
	) {
		Optional<String> fakeName = NamePresentation.fakeName(
				entry.profileId(),
				entry.profile().name()
		);
		Optional<SkinAssignment> skin = SkinPresentation.assignment(
				entry.profileId(),
				entry.profile().name()
		);
		if (fakeName.isEmpty() && skin.isEmpty()) {
			return entry;
		}

		GameProfile profile = entry.profile();
		PropertyMap properties = skin
				.map(assignment -> SkinPresentation.apply(
						profile.properties(),
						assignment
				))
				.orElse(profile.properties());
		GameProfile presentedProfile = new GameProfile(
				profile.id(),
				fakeName.orElse(profile.name()),
				properties
		);

		return new ClientboundPlayerInfoUpdatePacket.Entry(
				entry.profileId(),
				presentedProfile,
				entry.listed(),
				entry.latency(),
				entry.gameMode(),
				fakeName.<Component>map(Component::literal)
						.orElse(entry.displayName()),
				entry.showHat(),
				entry.listOrder(),
				entry.chatSession()
		);
	}

	public static void refresh(UUID uuid) {
		if (server != null) {
			refresh(server, List.of(uuid));
		}
	}

	public static void refreshSkin(UUID uuid) {
		if (server != null) {
			refreshSkins(server, List.of(uuid));
		}
	}

	public static void refresh(
			MinecraftServer minecraftServer,
			Collection<UUID> uuids
	) {
		refresh(minecraftServer, uuids, false);
	}

	public static void refreshSkins(
			MinecraftServer minecraftServer,
			Collection<UUID> uuids
	) {
		refresh(minecraftServer, uuids, true);
	}

	private static void refresh(
			MinecraftServer minecraftServer,
			Collection<UUID> uuids,
			boolean refreshLocalSkin
	) {
		List<ServerPlayer> players = uuids.stream()
				.distinct()
				.map(minecraftServer.getPlayerList()::getPlayer)
				.filter(Objects::nonNull)
				.toList();

		if (players.isEmpty()) {
			return;
		}

		List<UUID> onlineUuids = players.stream()
				.map(ServerPlayer::getUUID)
				.toList();
		minecraftServer.getPlayerList().broadcastAll(
				new ClientboundPlayerInfoRemovePacket(onlineUuids)
		);
		minecraftServer.getPlayerList().broadcastAll(
				ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(players)
		);
		players.forEach(PlayerProfilePresentation::refreshRemoteEntity);
		if (refreshLocalSkin) {
			players.forEach(player ->
					refreshLocalSkin(minecraftServer, player)
			);
		}
	}

	/*
	 * Rewriting PlayerInfo profiles is the strongest server-only nametag solution:
	 * clients render the fake profile name while the server retains the real one.
	 * Scoreboard objective/team entry keys still use real usernames because changing
	 * those keys would break vanilla command and scoreboard identity semantics.
	 */

	/*
	 * Remote player entities cache their PlayerInfo, so they are re-paired after
	 * the profile update to make name and skin changes visible immediately.
	 */
	private static void refreshRemoteEntity(ServerPlayer player) {
		Object trackedEntity = ((ChunkMapAccessor) (Object) player.level()
				.getChunkSource()
				.chunkMap)
				.ashmare$getEntityMap()
				.get(player.getId());
		if (!(trackedEntity instanceof TrackedEntityAccessor tracked)) {
			return;
		}

		ServerEntity serverEntity = tracked.ashmare$getServerEntity();
		List<ServerPlayer> viewers = tracked.ashmare$getSeenBy()
				.stream()
				.map(connection -> connection.getPlayer())
				.filter(viewer -> viewer != player)
				.toList();
		viewers.forEach(serverEntity::removePairing);
		viewers.forEach(serverEntity::addPairing);
	}

	/*
	 * AbstractClientPlayer caches PlayerInfo, including its skin. Rebuilding the
	 * local player with a same-dimension KEEP_ALL_DATA respawn is the only
	 * server-only way to invalidate that cache without requiring a reconnect.
	 * Vanilla closes an open container screen during this rebuild, but the server
	 * player and all gameplay state remain untouched and are resynchronized below.
	 */
	private static void refreshLocalSkin(
			MinecraftServer minecraftServer,
			ServerPlayer player
	) {
		Entity vehicle = player.getVehicle();
		Entity camera = player.getCamera();
		FoodData food = player.getFoodData();

		player.connection.send(new ClientboundRespawnPacket(
				player.createCommonSpawnInfo(player.level()),
				ClientboundRespawnPacket.KEEP_ALL_DATA
		));
		player.connection.teleport(
				player.getX(),
				player.getY(),
				player.getZ(),
				player.getYRot(),
				player.getXRot()
		);
		player.connection.send(new ClientboundSetHealthPacket(
				player.getHealth(),
				food.getFoodLevel(),
				food.getSaturationLevel()
		));
		player.connection.send(new ClientboundSetExperiencePacket(
				player.experienceProgress,
				player.totalExperience,
				player.experienceLevel
		));
		player.onUpdateAbilities();
		minecraftServer.getPlayerList().sendActivePlayerEffects(player);
		minecraftServer.getPlayerList().sendAllPlayerInfo(player);

		if (vehicle != null) {
			player.connection.send(new ClientboundSetPassengersPacket(vehicle));
		}
		if (camera != player) {
			player.connection.send(new ClientboundSetCameraPacket(camera));
		}

		player.connection.send(new ClientboundGameEventPacket(
				ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START,
				0.0F
		));
	}
}
