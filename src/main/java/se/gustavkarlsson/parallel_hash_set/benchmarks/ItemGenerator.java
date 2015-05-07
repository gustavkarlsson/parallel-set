package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;

public interface ItemGenerator {

    Collection<?> generate(int count);

    String getName();
}
