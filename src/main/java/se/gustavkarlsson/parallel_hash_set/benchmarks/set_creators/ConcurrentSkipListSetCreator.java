package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class ConcurrentSkipListSetCreator<T> implements SetCreator {

    @Override
    public String getName() {
        return "ConcurrentSkipListSet";
    }

    @Override
    public Set<Object> create(int capacity) {
        return new ConcurrentSkipListSet<>();
    }
}
