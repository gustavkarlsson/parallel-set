package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Collection;
import java.util.Set;

public class SetTest implements Test {

    private final SetCreator creator;
    private final SetOperation operation;
    private final ItemGenerator itemGenerator;
    private final int itemCount;

    private Collection<?> input;

    public SetTest(SetCreator creator, SetOperation operation, ItemGenerator itemGenerator, int itemCount) {
        this.creator = creator;
        this.operation = operation;
        this.itemGenerator = itemGenerator;
        this.itemCount = itemCount;
    }

    @Override
    public void prepare() {
        input = itemGenerator.generate(itemCount);
    }

    @Override
    public Runnable createExecution() {
        Set<Object> set = creator.create(itemCount);
        operation.prepare(input, set);
        return () -> operation.call(input, set);
    }

    @Override
    public String getDescription() {
        Object[] arguments = { creator.getName(), operation.getName(), itemGenerator.getName(), itemCount };
        return String.format("Set: %-26s Operation: %-11s Item: %-20s Count: %-9s", arguments);
    }
}
