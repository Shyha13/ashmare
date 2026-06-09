package com.arsh.ashmare.names;

import java.util.Objects;
import java.util.UUID;

public record FakeNameAssignment(
		UUID uuid,
		String lastKnownUsername,
		String fakeName
) {
	public FakeNameAssignment {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(lastKnownUsername, "lastKnownUsername");
		Objects.requireNonNull(fakeName, "fakeName");
	}
}
