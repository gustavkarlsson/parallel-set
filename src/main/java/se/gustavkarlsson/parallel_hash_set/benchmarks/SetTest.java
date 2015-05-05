package se.gustavkarlsson.parallel_hash_set.benchmarks;


import java.util.Collection;
import java.util.Set;

public class SetTest<T> implements Test {

	private final SetCreator<T> creator;
	private final SetMethod<T> method;
	private final ItemGenerator<T> itemGenerator;
	private final int itemCount;
	private final int coreCount;

	private Collection<T> input;

	public SetTest(SetCreator<T> creator, SetMethod<T> method, ItemGenerator<T> itemGenerator, int itemCount, int coreCount) {
		this.creator = creator;
		this.method = method;
		this.itemGenerator = itemGenerator;
		this.itemCount = itemCount;
		this.coreCount = coreCount;
	}

	@Override
	public void prepare() {
		input = itemGenerator.generate(itemCount);
		setParallelism(coreCount);
	}

	@Override
	public Runnable createExecution() {
		Set<T> set = creator.create(itemCount);
		method.prepare(input, set);
		return () -> method.call(input, set);
	}

	private void setParallelism(int parallelism) {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(parallelism));
	}

	@Override
	public String getDescription() {
		Object[] arguments = {creator.getName(), method.getName(), itemGenerator.getName(), itemCount, coreCount};
		return String.format("Set: %-21s Method: %-11s Item: %-20s Count: %-9s Cores: %s", arguments);
	}
}
