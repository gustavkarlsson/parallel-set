package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Set;

import se.gustavkarlsson.parallel_hash_set.ParallelHashSet;
import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class ParallelHashSetCreator<T> implements SetCreator {

    @Override
    public String getName() {
        return "ParallelHashSet";
    }

    @Override
    public Set<Object> create(int capacity) {
        return new ParallelHashSet<>(capacity);
    }
}
