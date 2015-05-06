package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Set;

public interface SetCreator<T> {

    Set<T> create(int capacity);

    String getName();
}
