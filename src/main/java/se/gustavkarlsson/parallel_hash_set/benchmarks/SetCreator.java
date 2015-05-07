package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Set;

public interface SetCreator {

    Set<Object> create(int capacity);

    String getName();
}
