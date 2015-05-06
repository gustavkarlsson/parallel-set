package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Set;

import se.gustavkarlsson.parallel_hash_set.ParallelConcurrentHashSet;
import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class ParallelConcurrentHashSetCreator<T> implements SetCreator<T> {

    @Override
    public Set<T> create(int capacity) {
        return new ParallelConcurrentHashSet<T>(capacity);
    }

    @Override
    public String getName() {
        return "ParallelConcurrentHashSet";
    }

}
