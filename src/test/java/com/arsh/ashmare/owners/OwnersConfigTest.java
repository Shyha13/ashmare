package com.arsh.ashmare.owners;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OwnersConfigTest {
	private static final UUID OWNER_UUID =
			UUID.fromString("5ba4d6db-5b73-4a33-a451-b16070bc6e9c");

	@Test
	void ownersDoNotBypassNamesOrSkinsByDefault() {
		OwnersConfig config = OwnersConfig.firstStartupDefault();
		OwnerEntry owner = config.owners().getFirst();

		assertFalse(owner.bypassNameRandomization());
		assertFalse(owner.bypassSkinRandomization());
	}

	@Test
	void existingOwnerJsonMigratesWithBypassesOff() {
		String json = """
				{
				  "owners": [
				    {
				      "uuid": "5ba4d6db-5b73-4a33-a451-b16070bc6e9c",
				      "lastKnownUsername": "Shyha"
				    }
				  ]
				}
				""";

		OwnerEntry owner = new Gson()
				.fromJson(json, OwnersConfig.class)
				.owners()
				.getFirst();
		assertFalse(owner.bypassNameRandomization());
		assertFalse(owner.bypassSkinRandomization());
	}

	@Test
	void bypassesToggleIndependently() {
		OwnersConfig config = new OwnersConfig();
		config.add(OWNER_UUID, "Shyha");

		OwnerEntry nameEnabled = config.toggleBypass(
				OWNER_UUID,
				"Shyha",
				OwnerBypass.NAME
		).orElseThrow();
		assertTrue(nameEnabled.bypassNameRandomization());
		assertFalse(nameEnabled.bypassSkinRandomization());

		OwnerEntry bothEnabled = config.toggleBypass(
				OWNER_UUID,
				"Shyha",
				OwnerBypass.SKIN
		).orElseThrow();
		assertTrue(bothEnabled.bypassNameRandomization());
		assertTrue(bothEnabled.bypassSkinRandomization());
	}

	@Test
	void identityRefreshPreservesBypasses() {
		OwnersConfig config = new OwnersConfig();
		config.add(OWNER_UUID, "Shyha");
		config.toggleBypass(OWNER_UUID, "Shyha", OwnerBypass.SKIN);

		assertTrue(config.observe(OWNER_UUID, "ShyhaRenamed"));
		OwnerEntry refreshed = config.find(
				OWNER_UUID,
				"ShyhaRenamed"
		).orElseThrow();
		assertFalse(refreshed.bypassNameRandomization());
		assertTrue(refreshed.bypassSkinRandomization());
	}
}
