package se.gustavkarlsson.parallel_hash_set;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ParallelConcurrentHashSet<T> implements Set<T> {

    private final Set<T> wrapped;

    public ParallelConcurrentHashSet() {
        wrapped = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
    }

    public ParallelConcurrentHashSet(int initialCapacity) {
        wrapped = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>(initialCapacity));
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return wrapped.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return wrapped.iterator();
    }

    @Override
    public Object[] toArray() {
        return wrapped.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return wrapped.toArray(a);
    }

    @Override
    public boolean add(T e) {
        return wrapped.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return wrapped.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.parallelStream().allMatch(o -> wrapped.contains(o));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return c.parallelStream().filter(e -> wrapped.add(e)).count() > 0;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return wrapped.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.parallelStream().filter(e -> wrapped.remove(e)).count() < 0;
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

}
