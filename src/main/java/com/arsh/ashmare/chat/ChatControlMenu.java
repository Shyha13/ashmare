package com.arsh.ashmare.chat;

import com.arsh.ashmare.config.AshmareConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public final class ChatControlMenu extends ChestMenu {
	private static final int ROWS = 3;
	private static final int SIZE = ROWS * 9;
	private static final Component TITLE = Component.literal("Ashmare Chat Controls");

	private final SimpleContainer controls;

	private ChatControlMenu(
			int containerId,
			Inventory playerInventory,
			SimpleContainer controls
	) {
		super(MenuType.GENERIC_9x3, containerId, playerInventory, controls, ROWS);
		this.controls = controls;
		refresh();
	}

	public static boolean open(ServerPlayer player) {
		OptionalInt containerId = player.openMenu(new SimpleMenuProvider(
				(id, inventory, menuPlayer) -> new ChatControlMenu(
						id,
						inventory,
						new SimpleContainer(SIZE)
				),
				TITLE
		));
		return containerId.isPresent();
	}

	@Override
	public void clicked(
			int slotId,
			int button,
			ClickType clickType,
			Player player
	) {
		if (slotId >= 0 && slotId < SIZE) {
			ChatToggle toggle = ChatToggle.fromSlot(slotId);
			if (toggle != null) {
				AshmareConfig.chat().update(config -> toggle.toggle(config));
				refresh();
			} else {
				broadcastChanges();
			}
			return;
		}

		super.clicked(slotId, button, clickType, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotId) {
		return ItemStack.EMPTY;
	}

	private void refresh() {
		controls.clearContent();
		ChatConfig config = AshmareConfig.chat().get();

		for (ChatToggle toggle : ChatToggle.values()) {
			boolean enabled = toggle.enabled(config);
			controls.setItem(toggle.slot(), createToggleItem(toggle, enabled));
		}

		broadcastChanges();
	}

	private static ItemStack createToggleItem(ChatToggle toggle, boolean enabled) {
		ItemStack stack = new ItemStack(enabled ? Items.LIME_DYE : Items.RED_DYE);
		ChatFormatting statusColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;

		stack.set(
				DataComponents.CUSTOM_NAME,
				Component.literal(toggle.label() + ": " + (enabled ? "ON" : "OFF"))
						.withStyle(statusColor, ChatFormatting.BOLD)
		);

		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal(
				enabled ? "Click to turn OFF." : "Click to turn ON."
		).withStyle(ChatFormatting.GRAY));

		if (toggle == ChatToggle.DEATH_MESSAGES) {
			lore.add(Component.literal(
					"Required for deathban system."
			).withStyle(ChatFormatting.YELLOW));
		}

		stack.set(DataComponents.LORE, new ItemLore(lore));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, enabled);
		return stack;
	}

	private enum ChatToggle {
		ADVANCEMENTS(10, "Advancement Announcements") {
			@Override
			boolean enabled(ChatConfig config) {
				return config.advancementAnnouncements();
			}

			@Override
			void toggle(ChatConfig config) {
				config.setAdvancementAnnouncements(!config.advancementAnnouncements());
			}
		},
		JOIN_LEAVE(12, "Join/Leave Messages") {
			@Override
			boolean enabled(ChatConfig config) {
				return config.joinLeaveMessages();
			}

			@Override
			void toggle(ChatConfig config) {
				config.setJoinLeaveMessages(!config.joinLeaveMessages());
			}
		},
		PLAYER_CHAT(13, "Player Chat Messages") {
			@Override
			boolean enabled(ChatConfig config) {
				return config.playerChatMessages();
			}

			@Override
			void toggle(ChatConfig config) {
				config.setPlayerChatMessages(!config.playerChatMessages());
			}
		},
		COMMAND_OUTPUT(14, "Command Output To Others") {
			@Override
			boolean enabled(ChatConfig config) {
				return config.commandOutputToOthers();
			}

			@Override
			void toggle(ChatConfig config) {
				config.setCommandOutputToOthers(!config.commandOutputToOthers());
			}
		},
		DEATH_MESSAGES(16, "Death Messages") {
			@Override
			boolean enabled(ChatConfig config) {
				return config.deathMessages();
			}

			@Override
			void toggle(ChatConfig config) {
				config.setDeathMessages(!config.deathMessages());
			}
		};

		private final int slot;
		private final String label;

		ChatToggle(int slot, String label) {
			this.slot = slot;
			this.label = label;
		}

		int slot() {
			return slot;
		}

		String label() {
			return label;
		}

		abstract boolean enabled(ChatConfig config);

		abstract void toggle(ChatConfig config);

		static ChatToggle fromSlot(int slot) {
			for (ChatToggle toggle : values()) {
				if (toggle.slot == slot) {
					return toggle;
				}
			}
			return null;
		}
	}
}
