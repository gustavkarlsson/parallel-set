package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class ConcurrentSkipListSetCreator<T> implements SetCreator<T> {

    @Override
    public String getName() {
        return "ConcurrentSkipListSet";
    }

    @Override
    public Set<T> create(int capacity) {
        return new ConcurrentSkipListSet<>();
    }
}
