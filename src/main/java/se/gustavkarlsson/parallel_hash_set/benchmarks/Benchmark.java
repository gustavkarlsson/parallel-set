package se.gustavkarlsson.parallel_hash_set.benchmarks;

import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.RandomStringGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.AddAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.ContainsAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.RemoveAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.RetainAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.HashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelHashSetCreator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

public class Benchmark {

	private static List<SetCreator> setCreators = Arrays.asList(
			//new TreeSetCreator(),
			//new ConcurrentSkipListSetCreator(),
			new HashSetCreator(),
			new ConcurrentHashSetCreator(),
			new ParallelConcurrentHashSetCreator(),
			new ParallelHashSetCreator()
	);

	private static List<SetOperation> setOperations = Arrays.asList(
			//new RemoveIf()
			new AddAll(),
			new RemoveAll(),
			new ContainsAll(),
			new RetainAll()
	);

	private static List<ItemGenerator> itemGenerators = Arrays.asList(
			// new ExpensiveGenerator()
			new RandomStringGenerator()
	);

	private static List<Integer> itemsCounts = Arrays.asList(
			//10_000
			1_000_000
	);

	private static final int ITERATIONS = 10;

	public static void main(String[] args) {
		if (args.length > 0) {
			int parallelism = Integer.parseInt(args[0]);
			System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(parallelism));
		}

		Stream<SetTest> tests = setOperations.stream().flatMap(setOperation ->
				setCreators.stream().flatMap(setCreator ->
						itemGenerators.stream().flatMap(itemGenerator ->
								itemsCounts.stream().map(itemCount ->
										new SetTest(setOperation, setCreator, itemGenerator, itemCount)))));

		System.out.println("Running benchmarks with parallelism: " + ForkJoinPool.getCommonPoolParallelism() + ", averaging results over " + ITERATIONS + " iterations\n");

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
		System.out.println("Time taken: " + timeTaken / count + "ms\n");
	}

	private static long measure(Runnable execution) {
		long start = System.currentTimeMillis();
		execution.run();
		long stop = System.currentTimeMillis();
		return stop - start;
	}
}
