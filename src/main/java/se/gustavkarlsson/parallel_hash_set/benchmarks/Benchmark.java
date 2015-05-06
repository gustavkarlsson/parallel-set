package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.Arrays;

import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.RandomStringGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.methods.AddAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.HashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelHashSetCreator;

public class Benchmark {

    public static void main(String[] args) {

        Test parallelHashSet = new SetTest<>(
                new ParallelHashSetCreator<>(),
                new AddAll<>(),
                new RandomStringGenerator(),
                1_000_000);

        Test parallelConcurrentHashSet = new SetTest<>(
                new ParallelConcurrentHashSetCreator<>(),
                new AddAll<>(),
                new RandomStringGenerator(),
                1_000_000);

        Test hashSet = new SetTest<>(
                new HashSetCreator<>(),
                new AddAll<>(),
                new RandomStringGenerator(),
                1_000_000);

        Test concurrentHashSet = new SetTest<>(
                new ConcurrentHashSetCreator<>(),
                new AddAll<>(),
                new RandomStringGenerator(),
                1_000_000);

        Arrays.asList(hashSet, concurrentHashSet, parallelConcurrentHashSet, parallelHashSet).forEach(t -> benchmark(t, 10));
    }

    private static void benchmark(Test test, int count) {
        System.out.println("Test: " + test.getDescription());
        test.prepare();
        test.createExecution().run(); // Warmup round
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
