package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Set;

import se.gustavkarlsson.parallel_hash_set.ParallelConcurrentHashSet;
import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class ParallelConcurrentHashSetCreator<T> implements SetCreator {

    @Override
    public Set<Object> create(int capacity) {
        return new ParallelConcurrentHashSet<>(capacity);
    }

    @Override
    public String getName() {
        return "ParallelConcurrentHashSet";
    }

}
