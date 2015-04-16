package se.gustavkarlsson;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


public class ParallelHashSet<T> implements Set<T> {

    private static final int DEFAULT_INITIAL_CAPACITY = 160;

    private Object[] table;
    private int size = 0;

    public ParallelHashSet(int initialCapacity) {
        table = new Object[initialCapacity];
    }

    public ParallelHashSet() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    @Override
    public boolean add(T t) {
        // TODO Implement
        return false;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Implement
        return false;
    }

    @Override
    public boolean contains(Object o) {
        // TODO Implement
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO Implement
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Implement
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        // TODO Implement
        return null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Implement
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        // TODO Implement
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Implement
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Implement
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        table = new Object[table.length];
    }
}
