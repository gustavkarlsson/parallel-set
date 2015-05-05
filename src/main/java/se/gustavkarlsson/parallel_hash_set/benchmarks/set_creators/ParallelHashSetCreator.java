package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import se.gustavkarlsson.parallel_hash_set.ParallelHashSet;
import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

import java.util.HashSet;
import java.util.Set;

public class ParallelHashSetCreator<T> implements SetCreator<T> {

	@Override
	public String getName() {
		return "ParallelHashSet";
	}

	@Override
	public Set<T> create(int capacity) {
		return new ParallelHashSet<>(capacity);
	}
}
