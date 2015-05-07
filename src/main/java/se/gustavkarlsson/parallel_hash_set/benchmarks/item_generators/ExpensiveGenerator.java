package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators;

import java.math.BigInteger;
import java.util.Random;

public class ExpensiveGenerator extends AbstractItemGenerator {

	private final Random random = new Random(0);

    @Override
    public String getName() {
        return "Expensive";
    }

	@Override
	protected Object generateItem() {
		return new Expensive(new BigInteger(130, random).toString(32));
	}

	private static class Expensive implements Comparable<Expensive> {

		private final String wrapped;

		public Expensive(String wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public String toString() {
			return wrapped;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Expensive) {
				Expensive other = (Expensive) obj;
				return wrapped.equals(other.wrapped);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = wrapped.hashCode();
			for (int i = 0; i < 1_000; i++) {
				hash += wrapped.hashCode();
			}
			return hash;
		}

		@Override
		public int compareTo(Expensive o) {
			return wrapped.compareTo(o.wrapped);
		}

	}
}
