package se.gustavkarlsson.parallel_hash_set.benchmarks;

import se.gustavkarlsson.parallel_hash_set.ParallelHashSet;
import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.RandomStringGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.methods.AddAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class Benchmark {

    public static void main(String[] args) throws IOException {

	    System.out.print("Press any key to start...");
	    System.in.read();

	    List<Test> tests = new ArrayList<>();

	    Test parallel = new SetTest<>(
			    new ParallelHashSetCreator<>(),
			    new AddAll<>(),
			    new RandomStringGenerator(),
			    1_000_000);
	    Test hash = new SetTest<>(
			    new HashSetCreator<>(),
			    new AddAll<>(),
			    new RandomStringGenerator(),
			    1_000_000);
	    Test concurrentSkipList = new SetTest<>(
			    new ConcurrentSkipListSetCreator<>(),
			    new AddAll<>(),
			    new RandomStringGenerator(),
			    1_000_000);
	    Test linked = new SetTest<>(
			    new LinkedHashSetCreator<>(),
			    new AddAll<>(),
			    new RandomStringGenerator(),
			    1_000_000);
	    Test tree = new SetTest<>(
			    new TreeSetCreator<>(),
			    new AddAll<>(),
			    new RandomStringGenerator(),
			    1_000_000);

	    benchmark(parallel, 100);

	    System.out.print("Press any key to stop...");
	    System.in.read();
    }

	private static void benchmark(Test test, int count) {
		System.gc();
		System.out.println("Test: " + test.getDescription());
		System.out.println("Preparing...");
		test.prepare();
		System.out.println("Warming up...");
		test.createExecution().run();
		System.out.println("Running...");
		long timeTaken = measure(test, count);
		System.out.println("Time taken: " + timeTaken + "ms");
		System.out.println("");
	}

	private static long measure(Test test, int count) {
		List<Runnable> executions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			executions.add(test.createExecution());
		}
		long start = System.currentTimeMillis();
		executions.forEach(Runnable::run);
		long stop = System.currentTimeMillis();
		return (stop - start) / count;
	}
}
