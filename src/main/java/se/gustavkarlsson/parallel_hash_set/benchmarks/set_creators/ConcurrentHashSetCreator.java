package se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetCreator;

public class ConcurrentHashSetCreator<T> implements SetCreator<T> {

    @Override
    public Set<T> create(int capacity) {
        return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>(capacity));
    }

    @Override
    public String getName() {
        return "ConcurrentHashSet";
    }

}
