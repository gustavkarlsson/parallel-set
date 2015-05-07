package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSetCreator<T> implements SetCreator {

    @Override
    public Set<Object> create(int capacity) {
        return Collections.newSetFromMap(new ConcurrentHashMap<>(capacity));
    }

    @Override
    public String getName() {
        return "ConcurrentHashSet";
    }

}
