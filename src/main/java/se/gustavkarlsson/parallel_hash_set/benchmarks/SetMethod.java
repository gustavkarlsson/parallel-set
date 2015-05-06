package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;
import java.util.Set;

public interface SetMethod<T> {

    void prepare(Collection<T> input, Set<T> set);

    void call(Collection<T> input, Set<T> set);

    String getName();
}
