package com.arsh.ashmare.owners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class OwnersConfig {
	/*
	 * Shyha is the default owner account for the Ashmare SMP. This value is
	 * used only by firstStartupDefault(); loading an existing owners.json never
	 * adds or restores the default owner.
	 */
	public static final String DEFAULT_OWNER_USERNAME = "Shyha";

	private List<OwnerEntry> owners = new ArrayList<>();

	public static OwnersConfig firstStartupDefault() {
		OwnersConfig config = new OwnersConfig();
		config.owners.add(new OwnerEntry(null, DEFAULT_OWNER_USERNAME));
		return config;
	}

	public List<OwnerEntry> owners() {
		ensureOwners();
		return List.copyOf(owners);
	}

	public List<OwnerEntry> unresolvedOwners() {
		ensureOwners();
		return owners.stream()
				.filter(owner -> !owner.isResolved())
				.toList();
	}

	public boolean isOwner(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensureOwners();
		return owners.stream()
				.anyMatch(owner -> uuid.equals(owner.uuid()));
	}

	public boolean isOwner(UUID uuid, String username) {
		Objects.requireNonNull(uuid, "uuid");
		ensureOwners();
		return owners.stream().anyMatch(owner -> owner.matches(uuid, username));
	}

	public Optional<OwnerEntry> find(UUID uuid, String username) {
		Objects.requireNonNull(uuid, "uuid");
		ensureOwners();
		return owners.stream()
				.filter(owner -> owner.matches(uuid, username))
				.findFirst();
	}

	public Optional<OwnerEntry> find(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensureOwners();
		return owners.stream()
				.filter(owner -> uuid.equals(owner.uuid()))
				.findFirst();
	}

	public boolean add(UUID uuid, String username) {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(username, "username");
		ensureOwners();

		Optional<OwnerEntry> existing = owners.stream()
				.filter(owner ->
						uuid.equals(owner.uuid())
								|| owner.lastKnownUsername()
										.equalsIgnoreCase(username)
				)
				.findFirst();
		owners.removeIf(owner ->
				uuid.equals(owner.uuid())
						|| owner.lastKnownUsername().equalsIgnoreCase(username)
		);
		owners.add(existing
				.map(owner -> owner.withIdentity(uuid, username))
				.orElseGet(() -> new OwnerEntry(uuid, username)));
		sortOwners();
		return existing.isEmpty();
	}

	public boolean remove(String usernameOrUuid) {
		Objects.requireNonNull(usernameOrUuid, "usernameOrUuid");
		ensureOwners();
		return owners.removeIf(owner -> owner.matches(usernameOrUuid));
	}

	public boolean resolve(
			String lookupUsername,
			UUID uuid,
			String canonicalUsername
	) {
		Objects.requireNonNull(lookupUsername, "lookupUsername");
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(canonicalUsername, "canonicalUsername");
		ensureOwners();

		Optional<OwnerEntry> matchingOwner = owners.stream().filter(owner ->
				owner.uuid() == null
						&& owner.lastKnownUsername().equalsIgnoreCase(lookupUsername)
		).findFirst();
		if (matchingOwner.isEmpty()) {
			return false;
		}

		owners.removeIf(owner ->
				uuid.equals(owner.uuid())
						|| (
								owner.uuid() == null
										&& owner.lastKnownUsername()
												.equalsIgnoreCase(lookupUsername)
						)
		);
		owners.add(matchingOwner.get().withIdentity(uuid, canonicalUsername));
		sortOwners();
		return true;
	}

	public boolean observe(UUID uuid, String currentUsername) {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(currentUsername, "currentUsername");
		Optional<OwnerEntry> matchingOwner = find(uuid, currentUsername);
		if (matchingOwner.isEmpty()) {
			return false;
		}

		OwnerEntry existing = matchingOwner.get();
		if (
				uuid.equals(existing.uuid())
						&& currentUsername.equals(existing.lastKnownUsername())
		) {
			return false;
		}

		owners.removeIf(owner ->
				uuid.equals(owner.uuid()) || owner == existing
		);
		owners.add(existing.withIdentity(uuid, currentUsername));
		sortOwners();
		return true;
	}

	public Optional<OwnerEntry> toggleBypass(
			UUID uuid,
			String username,
			OwnerBypass bypass
	) {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(username, "username");
		Objects.requireNonNull(bypass, "bypass");
		ensureOwners();

		Optional<OwnerEntry> matchingOwner = find(uuid, username);
		if (matchingOwner.isEmpty()) {
			return Optional.empty();
		}

		OwnerEntry existing = matchingOwner.get();
		OwnerEntry updated = existing.withBypass(
				bypass,
				!existing.bypasses(bypass)
		);
		owners.remove(existing);
		owners.add(updated);
		sortOwners();
		return Optional.of(updated);
	}

	private void ensureOwners() {
		if (owners == null) {
			owners = new ArrayList<>();
		}
	}

	private void sortOwners() {
		owners.sort(Comparator.comparing(
				OwnerEntry::lastKnownUsername,
				String.CASE_INSENSITIVE_ORDER
		));
	}
}
