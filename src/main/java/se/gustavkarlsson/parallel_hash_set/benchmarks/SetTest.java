package se.gustavkarlsson.parallel_hash_set.benchmarks;


import java.util.Collection;
import java.util.Set;

public class SetTest<T> implements Test {

	private final SetCreator<T> creator;
	private final SetMethod<T> method;
	private final ItemGenerator<T> itemGenerator;
	private final int itemCount;

	private Collection<T> input;

	public SetTest(SetCreator<T> creator, SetMethod<T> method, ItemGenerator<T> itemGenerator, int itemCount) {
		this.creator = creator;
		this.method = method;
		this.itemGenerator = itemGenerator;
		this.itemCount = itemCount;
	}

	@Override
	public void prepare() {
		input = itemGenerator.generate(itemCount);
	}

	@Override
	public Runnable createExecution() {
		Set<T> set = creator.create(itemCount);
		method.prepare(input, set);
		return () -> method.call(input, set);
	}

	@Override
	public String getDescription() {
		Object[] arguments = {creator.getName(), method.getName(), itemGenerator.getName(), itemCount};
		return String.format("Set: %-21s Method: %-11s Item: %-20s Count: %-9s", arguments);
	}
}
