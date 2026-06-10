package com.arsh.ashmare.owners;

public enum OwnerBypass {
	NAME("name"),
	SKIN("skin");

	private final String commandName;

	OwnerBypass(String commandName) {
		this.commandName = commandName;
	}

	public String commandName() {
		return commandName;
	}
}
