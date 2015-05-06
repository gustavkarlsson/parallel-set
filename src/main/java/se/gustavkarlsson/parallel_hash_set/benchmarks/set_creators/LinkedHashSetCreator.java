package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.LinkedHashSet;
import java.util.Set;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class LinkedHashSetCreator<T> implements SetCreator<T> {

    @Override
    public String getName() {
        return "LinkedHashSet";
    }

    @Override
    public Set<T> create(int capacity) {
        return new LinkedHashSet<>(capacity);
    }
}
