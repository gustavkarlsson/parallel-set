package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import se.gustavkarlsson.parallel_hash_set.benchmarks.ItemGenerator;

import java.util.*;

public abstract class AbstractItemGenerator implements ItemGenerator {

	@Override
	public Collection<?> generate(int count) {
		Set<Object> items = new LinkedHashSet<>(count);
		for (int i = 0; i < count; i++) {
			items.add(generateItem());
		}
		return Collections.unmodifiableSet(items);
	}

	protected abstract Object generateItem();
}
