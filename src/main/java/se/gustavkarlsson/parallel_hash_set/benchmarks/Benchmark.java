package se.gustavkarlsson.parallel_hash_set.benchmarks;

import se.gustavkarlsson.parallel_hash_set.ParallelHashSet;

import java.math.BigInteger;
import java.util.*;

public class Benchmark {

    private static final int NUMBER_OF_ELEMENTS = 10_000_000;

    public static void main(String[] args) {
        System.out.println("Startup...");
        List<String> items = new ArrayList<>(NUMBER_OF_ELEMENTS);
        Random random = new Random(0);
        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
            items.add(new BigInteger(130, random).toString(32));
        }

        Set<String> parallel = new ParallelHashSet<>(NUMBER_OF_ELEMENTS);
        Set<String> sequential = new HashSet<>(NUMBER_OF_ELEMENTS);

        benchmark(sequential, items);
        benchmark(parallel, items);

    }

    private static <T> void benchmark(Set<T> testSet, Collection<T> items) {
        System.out.println("Testing: " + testSet.getClass().getSimpleName());
        System.out.println("Benchmark...");
        long start = System.currentTimeMillis();
        testSet.addAll(items);
        long stop = System.currentTimeMillis();
        long diff = stop - start;
        System.out.println("Class: " + testSet.getClass().getSimpleName() + ", Time: " + diff);
    }
}
