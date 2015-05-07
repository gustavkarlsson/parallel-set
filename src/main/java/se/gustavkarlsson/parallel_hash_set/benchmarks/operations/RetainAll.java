package se.gustavkarlsson.parallel_hash_set.benchmarks.operations;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetOperation;

import java.util.Collection;
import java.util.Set;

public class RetainAll implements SetOperation {

    @Override
    public void prepare(Collection<?> input, Set<Object> set) {
        set.addAll(input);
    }

    @Override
    public void call(Collection<?> input, Set<Object> set) {
        set.retainAll(input);
    }

    @Override
    public String getName() {
        return "retainAll";
    }
}
