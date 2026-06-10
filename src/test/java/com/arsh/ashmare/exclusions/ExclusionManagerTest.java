package com.arsh.ashmare.exclusions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExclusionManagerTest {
	@Test
	void exclusionsBlockNormalPlayers() {
		assertTrue(ExclusionManager.blocksIdentityRandomization(true, false));
	}

	@Test
	void ownerBypassSettingsOverrideLegacyExclusionEntries() {
		assertFalse(ExclusionManager.blocksIdentityRandomization(true, true));
	}

	@Test
	void includedPlayersAreNotBlocked() {
		assertFalse(ExclusionManager.blocksIdentityRandomization(false, false));
		assertFalse(ExclusionManager.blocksIdentityRandomization(false, true));
	}
}
