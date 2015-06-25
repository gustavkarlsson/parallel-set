package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;
import java.util.Set;

public class SetTest implements Test {

	private final SetOperation operation;
    private final SetCreator creator;
    private final ItemProvider itemProvider;
    private final int itemCount;

    private Collection<?> input;

    public SetTest(SetOperation operation, SetCreator creator, ItemProvider itemProvider, int itemCount) {
	    this.operation = operation;
        this.creator = creator;
        this.itemProvider = itemProvider;
        this.itemCount = itemCount;
    }

    @Override
    public void prepare() {
        input = itemProvider.getItems(itemCount);
    }

    @Override
    public Runnable createExecution() {
        Set<Object> set = creator.create(itemCount);
        operation.prepare(input, set);
        return () -> operation.call(input, set);
    }

    @Override
    public String getDescription() {
        Object[] arguments = { operation.getName(), creator.getName(), itemProvider.getName(), itemCount };
        return String.format("Operation: %-11s Set: %-26s Item: %-20s Count: %-9s", arguments);
    }
}
