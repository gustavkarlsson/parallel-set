package se.gustavkarlsson.parallel_hash_set.benchmarks.operations;

import se.gustavkarlsson.parallel_hash_set.benchmarks.SetOperation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public class RemoveIf implements SetOperation {

	private final Predicate predicate = new Predicate<Object>() {
		@Override
		public boolean test(Object o) {
			return false;
		}
	};

    @Override
    public void prepare(Collection<?> input, Set<Object> set) {
        set.addAll(input);
    }

    @Override
    public void call(Collection<?> input, Set<Object> set) {
        set.removeIf(new EveryOther());
    }

    @Override
    public String getName() {
        return "removeIf";
    }

	private static class EveryOther implements Predicate<Object> {

		private boolean toggle;

		@Override
		public boolean test(Object o) {
			return toggle = !toggle;
		}
	}
}
