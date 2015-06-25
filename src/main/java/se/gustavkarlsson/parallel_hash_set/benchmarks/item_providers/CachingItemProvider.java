package se.gustavkarlsson.parallel_hash_set.benchmarks.item_providers;

import se.gustavkarlsson.parallel_hash_set.benchmarks.ItemProvider;

import java.util.*;

public abstract class CachingItemProvider implements ItemProvider {

	private Map<Integer, Collection<Object>> cache = new HashMap<>();

	@Override
	public Collection<?> getItems(int count) {
		if (!cache.containsKey(count)) {
			cache.put(count, generateItems(count));
		}
		return cache.get(count);
	}

	private Collection<Object> generateItems(int count) {
		Collection<Object> uniqueItems = new LinkedHashSet<>(count);
		for (int i = 0; i < count; i++) {
			uniqueItems.add(createItem());
		}
		while (uniqueItems.size() < count) {
			uniqueItems.add(createItem());
		}
		List<Object> items = new ArrayList<>(count);
		items.addAll(uniqueItems);
		return items;
	}

	protected abstract Object createItem();
}
