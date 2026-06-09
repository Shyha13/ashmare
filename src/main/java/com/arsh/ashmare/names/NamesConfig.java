package com.arsh.ashmare.names;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class NamesConfig {
	private List<FakeNameAssignment> assignments = new ArrayList<>();

	public List<FakeNameAssignment> assignments() {
		ensureAssignments();
		return List.copyOf(assignments);
	}

	public Optional<FakeNameAssignment> find(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensureAssignments();
		return assignments.stream()
				.filter(assignment -> assignment.uuid().equals(uuid))
				.findFirst();
	}

	public Optional<String> fakeName(UUID uuid) {
		return find(uuid).map(FakeNameAssignment::fakeName);
	}

	public void removeAll(Set<UUID> uuids) {
		Objects.requireNonNull(uuids, "uuids");
		ensureAssignments();
		assignments.removeIf(assignment -> uuids.contains(assignment.uuid()));
	}

	public boolean remove(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid");
		ensureAssignments();
		return assignments.removeIf(assignment -> assignment.uuid().equals(uuid));
	}

	public int clear() {
		ensureAssignments();
		int removed = assignments.size();
		assignments.clear();
		return removed;
	}

	public void put(FakeNameAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment");
		ensureAssignments();
		assignments.removeIf(existing -> existing.uuid().equals(assignment.uuid()));
		assignments.add(assignment);
		assignments.sort(Comparator.comparing(
				FakeNameAssignment::lastKnownUsername,
				String.CASE_INSENSITIVE_ORDER
		));
	}

	private void ensureAssignments() {
		if (assignments == null) {
			assignments = new ArrayList<>();
		}
	}
}
