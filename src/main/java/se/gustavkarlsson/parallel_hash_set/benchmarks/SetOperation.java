package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;
import java.util.Set;

public interface SetOperation {

    void prepare(Collection<?> input, Set<Object> set);

    void call(Collection<?> input, Set<Object> set);

    String getName();
}
