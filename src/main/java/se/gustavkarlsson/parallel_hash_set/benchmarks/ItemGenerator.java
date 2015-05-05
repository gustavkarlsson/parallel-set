package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;

public interface ItemGenerator<T> {

	Collection<T> generate(int count);

	String getName();
}
