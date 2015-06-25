package se.gustavkarlsson.parallel_hash_set.benchmarks;

import se.gustavkarlsson.parallel_hash_set.benchmarks.item_providers.RandomStringProvider;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.AddAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.ContainsAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.RemoveAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelHashSetCreator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Benchmark {

	public static final String PARALLELISM_PROPERTY = "java.util.concurrent.ForkJoinPool.common.parallelism";

	private static List<SetCreator> setCreators = Arrays.asList(
			//new TreeSetCreator(),
			//new ConcurrentSkipListSetCreator(),
			//new HashSetCreator(),
			//new ConcurrentHashSetCreator(),
			new ParallelConcurrentHashSetCreator(),
			new ParallelHashSetCreator()
	);

	private static List<SetOperation> setOperations = Arrays.asList(
			//new RemoveIf()
			new AddAll(),
			new RemoveAll(),
			new ContainsAll()
			//new RetainAll()
	);

	private static List<ItemProvider> itemProviders = Arrays.asList(
			// new ExpensiveGenerator()
			new RandomStringProvider()
	);

	private static List<Integer> itemsCounts = Arrays.asList(
			1_000,
			2_000,
			4_000,
			8_000,
			16_000,
			32_000,
			64_000,
			128_000,
			256_000
	);

	private static final int ITERATIONS = 100;

	public static void main(String[] args) {
		if (args.length > 0) {
			int parallelism = Integer.parseInt(args[0]);
			System.setProperty(PARALLELISM_PROPERTY, String.valueOf(parallelism));
		}

		Stream<SetTest> tests = setOperations.stream().flatMap(setOperation ->
				setCreators.stream().flatMap(setCreator ->
						itemProviders.stream().flatMap(itemGenerator ->
								itemsCounts.stream().map(itemCount ->
										new SetTest(setOperation, setCreator, itemGenerator, itemCount)))));

		System.out.println("Running benchmarks with parallelism: " + Integer.parseInt(System.getProperty(PARALLELISM_PROPERTY)) + ", averaging results over " + ITERATIONS + " iterations\n");

		long start = System.currentTimeMillis();
		tests.forEach(test -> benchmark(test, ITERATIONS));
		long stop = System.currentTimeMillis();
		System.out.println("Total time taken: " + (stop - start) / 1000 + "s");
	}

	private static void benchmark(Test test, int count) {
		System.out.println(test.getDescription());
		test.prepare();
		test.createExecution().run(); // Warm up round
		long timeTaken = 0;
		for (int i = 0; i < count; i++) {
			System.gc();
			Runnable execution = test.createExecution();
			timeTaken += measure(execution);
		}
		System.out.println("Time taken: " + (timeTaken / count) / 1_000 + " microseconds\n");
	}

	private static long measure(Runnable execution) {
		long start = System.nanoTime();
		execution.run();
		long stop = System.nanoTime();
		return stop - start;
	}
}
