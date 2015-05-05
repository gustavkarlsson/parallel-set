package se.gustavkarlsson.parallel_hash_set.benchmarks;


import java.util.Set;
import java.util.function.Function;

public class SetTest<T> implements Test {

	private final Function<Integer, Set<T>> creator;
	private final int itemCount;
	private final int cores;
	private final Runnable method;

	protected SetTest(Function<Integer, Set<T>> creator, int itemCount, int cores, Method method) {
		this.creator = creator;
		this.itemCount = itemCount;
		this.cores = cores;
		this.method = method;
	}

	@Override
	public Runnable getMethod() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}
}
