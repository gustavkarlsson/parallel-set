package se.gustavkarlsson.parallel_hash_set.benchmarks.item_providers;

import java.math.BigInteger;
import java.util.Random;

public class RandomStringProvider extends CachingItemProvider {

	private final Random random = new Random(0);

	@Override
	public String getName() {
		return "RandomString";
	}

	@Override
	protected Object createItem() {
		return new BigInteger(130, random).toString(32);
	}
}
