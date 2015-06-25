package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;

public interface ItemProvider {

    Collection<?> getItems(int count);

    String getName();
}
