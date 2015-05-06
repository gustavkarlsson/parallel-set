package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Set;
import java.util.TreeSet;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class TreeSetCreator<T> implements SetCreator<T> {

    @Override
    public String getName() {
        return "TreeSet";
    }

    @Override
    public Set<T> create(int capacity) {
        return new TreeSet<>();
    }
}
