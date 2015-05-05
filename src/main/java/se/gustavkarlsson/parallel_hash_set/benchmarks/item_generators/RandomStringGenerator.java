package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import se.gustavkarlsson.parallel_hash_set.benchmarks.ItemGenerator;

import java.util.Collection;

public class RandomStringGenerator implements ItemGenerator<String> {
	@Override
	public Collection<String> generate(int count) {
		return null;
	}

	@Override
	public String getName() {
		return "RandomString";
	}
}
