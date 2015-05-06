package se.gustavkarlsson.parallel_hash_set.benchmarks.item_generators.classes;

public class Expensive implements Comparable<Expensive> {

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
        return wrapped.equals(obj);
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
