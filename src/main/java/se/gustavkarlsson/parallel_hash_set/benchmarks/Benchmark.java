package se.gustavkarlsson.parallel_hash_set.benchmarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.ExpensiveGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.RandomStringGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.methods.AddAll;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ConcurrentSkipListSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.HashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.LinkedHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelConcurrentHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.ParallelHashSetCreator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.set_creators.TreeSetCreator;

public class Benchmark {

    public static void main(String[] args) {

        List<ItemGenerator<?>> itemGenerators = Arrays.asList(
                new RandomStringGenerator(),
                new ExpensiveGenerator());

        List<AddAll<?>> setMethods = Arrays.asList(
                new AddAll<>());

        List<SetCreator<?>> setCreators = Arrays.asList(
                new ConcurrentHashSetCreator<>(),
                new ConcurrentSkipListSetCreator<>(),
                new HashSetCreator<>(),
                new LinkedHashSetCreator<>(),
                new ParallelConcurrentHashSetCreator<>(),
                new ParallelHashSetCreator<>(),
                new TreeSetCreator<>());

        List<Test> tests = new ArrayList<Test>();

        setCreators.forEach(set ->
                itemGenerators.forEach(item ->
                        setMethods.forEach(method ->
                                tests.add(new SetTest(set, method, item, 1_000_000)))));

        tests.forEach(t -> benchmark(t, 10));
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
