package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import se.gustavkarlsson.parallel_hash_set.benchmarks.ItemGenerator;

public class RandomStringGenerator implements ItemGenerator<String> {

    @Override
    public Collection<String> generate(int count) {
        Random random = new Random(0);
        List<String> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            items.add(new BigInteger(130, random).toString(32));
        }
        return items;
    }

    @Override
    public String getName() {
        return "RandomString";
    }
}
