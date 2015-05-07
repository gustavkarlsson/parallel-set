package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import se.gustavkarlsson.parallel_hash_set.benchmarks.ItemGenerator;

import java.util.*;

public abstract class AbstractItemGenerator implements ItemGenerator {

	@Override
	public Collection<?> generate(int count) {
		List<Object> items = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			items.add(generateItem());
		}
		return Collections.unmodifiableList(items);
	}

	protected abstract Object generateItem();
}
