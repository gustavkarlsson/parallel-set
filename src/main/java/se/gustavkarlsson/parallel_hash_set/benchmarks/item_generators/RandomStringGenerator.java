package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import java.math.BigInteger;
import java.util.Random;

public class RandomStringGenerator extends AbstractItemGenerator {

	private final Random random = new Random(0);

	@Override
	public String getName() {
		return "RandomString";
	}

	@Override
	protected Object generateItem() {
		return new BigInteger(130, random).toString(32);
	}
}
