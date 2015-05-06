package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.HashSet;
import java.util.Set;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class HashSetCreator<T> implements SetCreator<T> {

    @Override
    public String getName() {
        return "HashSet";
    }

    @Override
    public Set<T> create(int capacity) {
        return new HashSet<>(capacity);
    }
}
