package com.arsh.ashmare.chat;

public final class ChatConfig {
	private boolean advancementAnnouncements = true;
	private boolean joinLeaveMessages = true;
	private boolean playerChatMessages = true;
	private boolean commandOutputToOthers = true;
	private boolean deathMessages = true;

	public boolean advancementAnnouncements() {
		return advancementAnnouncements;
	}

	public void setAdvancementAnnouncements(boolean enabled) {
		advancementAnnouncements = enabled;
	}

	public boolean joinLeaveMessages() {
		return joinLeaveMessages;
	}

	public void setJoinLeaveMessages(boolean enabled) {
		joinLeaveMessages = enabled;
	}

	public boolean playerChatMessages() {
		return playerChatMessages;
	}

	public void setPlayerChatMessages(boolean enabled) {
		playerChatMessages = enabled;
	}

	public boolean commandOutputToOthers() {
		return commandOutputToOthers;
	}

	public void setCommandOutputToOthers(boolean enabled) {
		commandOutputToOthers = enabled;
	}

	public boolean deathMessages() {
		return deathMessages;
	}

	public void setDeathMessages(boolean enabled) {
		deathMessages = enabled;
	}
}
