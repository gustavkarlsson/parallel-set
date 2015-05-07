package se.gustavkarlsson.parallel_hash_set.benchmarks;

import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.RandomStringGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.AddAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.ContainsAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.operations.RemoveAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelHashSetCreator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Benchmark {

	private static List<SetCreator> setCreators = Arrays.asList(
			/*new TreeSetCreator(),
			new HashSetCreator(),
			new LinkedHashSetCreator(),
			new ConcurrentHashSetCreator(),
			new ConcurrentSkipListSetCreator(),
			new ParallelConcurrentHashSetCreator(),*/
			new ParallelHashSetCreator()
	);

	private static List<SetOperation> setOperations = Arrays.asList(
			new AddAll(),
			new RemoveAll(),
			new ContainsAll()/*,
			new RetainAll()*/
	);

	private static List<ItemGenerator> itemGenerators = Arrays.asList(
			new RandomStringGenerator()/*,
			new ExpensiveGenerator()*/
	);

	private static List<Integer> itemsCounts = Arrays.asList(
			/*1000,
			10000,
			100000,
			1000000,*/
			10000000
	);

	public static void main(String[] args) {

		Stream<SetTest> tests = setCreators.stream().flatMap(setCreator ->
				setOperations.stream().flatMap(setOperation ->
						itemGenerators.stream().flatMap(itemGenerator ->
								itemsCounts.stream().map(itemCount ->
										new SetTest(setCreator, setOperation, itemGenerator, itemCount)))));

		int iterations = 1;
		System.out.println("Running benchmarks, averaging results over " + iterations + " iterations\n");

		long start = System.currentTimeMillis();
		tests.forEach(test -> benchmark(test, iterations));
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
