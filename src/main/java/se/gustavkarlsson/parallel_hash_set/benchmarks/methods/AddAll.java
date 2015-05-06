package se.gustavkarlsson.parallel_hash_set.benchmarks.methods;

import java.util.Collection;
import java.util.Set;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetMethod;

public class AddAll<T> implements SetMethod<T> {

    @Override
    public void prepare(Collection<T> input, Set<T> set) {
        // Do nothing
    }

    @Override
    public void call(Collection<T> input, Set<T> set) {
        set.addAll(input);
    }

    @Override
    public String getName() {
        return "addAll";
    }
}
