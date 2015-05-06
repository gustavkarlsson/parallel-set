package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import se.gustavkarlsson.parallel_hash_set.benchmarks.ItemGenerator;
import se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.classes.Expensive;

public class ExpensiveGenerator implements ItemGenerator<Expensive> {

    @Override
    public Collection<Expensive> generate(int count) {
        Random random = new Random(0);
        List<Expensive> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            items.add(new Expensive(new BigInteger(130, random).toString(32)));
        }
        return items;
    }

    @Override
    public String getName() {
        return "Expensive";
    }
}
