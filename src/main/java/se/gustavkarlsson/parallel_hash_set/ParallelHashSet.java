package se.gustavkarlsson.parallel_hash_set;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ParallelHashSet<T> extends AbstractSet<T> {

    private static final int MAXIMUM_CAPACITY = 1 << 30; // Must be a power of two
    private static final int DEFAULT_CAPACITY = 128;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final Object TOMBSTONE = new Object();

    private static final ForkJoinPool workers = ForkJoinPool.commonPool();

    private final float loadFactor;

    private AtomicReferenceArray<Object> table;
    private int size = 0;
    private int usedSlots = 0;

    public ParallelHashSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity must be non-negative");
        }
        if (loadFactor <= 0) {
            throw new IllegalArgumentException("Load factor must be positive");
        }
        this.loadFactor = loadFactor;
        table = createTable(initialCapacity);
    }

    public ParallelHashSet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public ParallelHashSet(float loadFactor) {
        this(DEFAULT_CAPACITY, loadFactor);
    }

    public ParallelHashSet() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public synchronized boolean add(T element) {
        if (element == null) {
            throw new NullPointerException("Cannot add null value");
        }
        resizeTableIfNeeded(1);
        int index = element.hashCode();
        while (true) {
            index &= table.length() - 1;
            final Object previous = table.get(index);
            if (previous == null) {
                // Free slot. Set element
                table.set(index, element);
                size++;
                usedSlots++;
                return true;
            }
            if (previous != TOMBSTONE && (element == previous || element.equals(previous))) {
                // Same or equal object. Element already exists
                return false;
            }
            // Slot occupied with another element or a tombstone
            index++; // Reprobe
        }
    }

    private boolean addConcurrent(T element) {
        if (element == null) {
            throw new NullPointerException("Cannot add null value");
        }
        int index = element.hashCode();
        while (true) {
            index &= table.length() - 1;
            final Object previous = table.get(index);
            if (previous == null && table.compareAndSet(index, null, element)) {
                // Slot was free and remained free during CAS
                return true;
            } else if (previous != TOMBSTONE && (element == previous || element.equals(previous))) {
                // Same or equal object. Element already exists
                return false;
            }
            // Slot occupied with another element or a tombstone
            index++; // Reprobe
        }
    }

    @Override
    public synchronized boolean remove(Object element) {
        if (element == null) {
            return false;
        }
        int index = element.hashCode();
        while (true) {
            index &= table.length() - 1;
            final Object existing = table.get(index);
            if (existing == null) {
                // Free slot. Element could not be found
                return false;
            }
            if (existing != TOMBSTONE && (element == existing || element.equals(existing))) {
                // Same or equal element. Set tombstone
                table.set(index, TOMBSTONE);
                size--;
                return true;
            }
            // Slot occupied with another element or a tombstone
            index++; // Reprobe
        }
    }

    private boolean removeConcurrent(Object element) {
        if (element == null) {
            return false;
        }
        int index = element.hashCode();
        while (true) {
            index &= table.length() - 1;
            final Object existing = table.get(index);
            if (existing == null) {
                // Free slot. Element could not be found
                return false;
            }
            if (existing != TOMBSTONE && (element == existing || element.equals(existing)) && table.compareAndSet(index, existing, TOMBSTONE)) {
                // Successfully replaced with tombstone
                return true;
            }
            // Slot occupied with another element or a tombstone
            index++; // Reprobe
        }
    }

    @Override
    public boolean contains(Object element) {
        if (element == null) {
            return false;
        }
        AtomicReferenceArray table = this.table;
        int index = element.hashCode();
        while (true) {
            index &= table.length() - 1;
            final Object existing = table.get(index);
            if (existing == null) {
                // Free slot. Element could not be found
                return false;
            }
            if (existing != TOMBSTONE && (element == existing || element.equals(existing))) {
                // Same or equal element
                return true;
            }
            // Slot occupied with another element or a tombstone
            index++; // Reprobe
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ParallelHashSetIterator<>(this);
    }

    @Override
    public synchronized Spliterator<T> spliterator() {
        return new ParallelHashSetSpliterator<>(this);
    }

    @Override
    public Object[] toArray() {
        return parallelStream().toArray();
    }

    @Override
    public synchronized <T1> T1[] toArray(T1[] array) {
        T1[] newArray = array.length >= size() ? array : (T1[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        Iterator<T> it = iterator();
        for (int i = 0; i < size(); i++) {
            newArray[i] = (T1) it.next();
        }
        if (newArray.length > size()) {
            newArray[size()] = null; // Null terminate
        }
        return newArray;
    }

    @Override
    public synchronized boolean containsAll(Collection<?> collection) {
        if (collection == null) {
            throw new NullPointerException("Supplied collection cannot be null");
        }
        return collection.parallelStream().allMatch(this::contains);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends T> collection) {
        if (collection == null) {
            throw new NullPointerException("Supplied collection cannot be null");
        }
        resizeTableIfNeeded(collection.size());
        int added = (int) collection.parallelStream().filter(this::addConcurrent).count();
        size += added;
        usedSlots += added;
        return added > 0;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> collection) {
        if (collection == null) {
            throw new NullPointerException("Supplied collection cannot be null");
        }
        int removed = (int) collection.parallelStream().filter(this::removeConcurrent).count();
        size -= removed;
        return removed > 0;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> collection) {
        if (collection == null) {
            throw new NullPointerException("Supplied collection cannot be null");
        }
        List<RetainAllSubTask> tasks = new ArrayList<>();
        int parallelism = workers.getParallelism();
        for (int i = 0; i < parallelism; i++) {
            tasks.add(new RetainAllSubTask(collection, i, parallelism));
        }
        List<Future<Integer>> futures = workers.invokeAll(tasks);
        int removed = 0;
        for (Future<Integer> future : futures) {
            try {
                removed += future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        size -= removed;
        return removed > 0;
    }

    private void resizeTableIfNeeded(int potentialElements) {
        float maxLoad = table.length() * loadFactor;
        int potentialLoad = usedSlots + potentialElements;
        if (potentialLoad > maxLoad) {
            resizeTable(size() + potentialElements);
        }
    }

    private void resizeTable(int minCapacity) {
        Stream<T> stream = parallelStream(); // Stream of current table contents
        table = createTable(minCapacity); // Switch table
        stream.forEach(this::addConcurrent); // Add elemets from stream to new table
    }

    private AtomicReferenceArray<Object> createTable(int minCapacity) {
        return new AtomicReferenceArray<Object>(calculateTableSize(minCapacity));
    }

    // Stolen from HashMap
    private final int calculateTableSize(int capacity) {
        int idealSize = (int) (capacity / loadFactor) + 1;
        int n = idealSize - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public synchronized void clear() {
        table = new AtomicReferenceArray<Object>(table.length());
        size = 0;
        usedSlots = 0;
    }

    @Override
    public int hashCode() {
        return parallelStream().collect(Collectors.summingInt(Objects::hashCode));
    }

    @Override
    public String toString() {
        return parallelStream().map(Objects::toString).collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super T> filter) {
        // TODO implement parallel removeIf(Predicate<? super T> filter)
        return super.removeIf(filter);
    }

    private class RetainAllSubTask implements Callable<Integer> {

        private final Collection<?> collection;
        private final int offset;
        private final int jump;

        public RetainAllSubTask(Collection<?> collection, int offset, int jump) {
            this.collection = collection;
            this.offset = offset;
            this.jump = jump;
        }

        @Override
        public Integer call() throws Exception {
            int removed = 0;
            for (int i = offset; i < table.length(); i += jump) {
                final Object element = table.get(i);
                if (element != null && element != TOMBSTONE && !collection.contains(element)) {
                    table.set(i, TOMBSTONE);
                    removed++;
                }
            }
            return removed;
        }
    }

    private static class ParallelHashSetIterator<T> implements Iterator<T> {

        private final AtomicReferenceArray<Object> table;

        private int nextIndex = 0;

        public ParallelHashSetIterator(ParallelHashSet set) {
            this.table = set.table;
        }

        @Override
        public boolean hasNext() {
            for (int i = nextIndex; i < table.length(); i++) {
                Object element = table.get(i);
                if (element != null && element != TOMBSTONE) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public synchronized T next() {
            while (nextIndex < table.length()) {
                Object element = table.get(nextIndex);
                nextIndex++;
                if (element != null && element != TOMBSTONE) {
                    return (T) element;
                }
            }
            throw new NoSuchElementException("No more elements");
        }
    }

    private static class ParallelHashSetSpliterator<T> implements Spliterator<T> {

        private final AtomicReferenceArray<Object> table;
        private final float fillRatio;

        private int nextIndex;
        private int fence;

        public ParallelHashSetSpliterator(ParallelHashSet set) {
            this.table = set.table;
            this.fillRatio = (float) set.size() / table.length();
            this.nextIndex = 0;
            this.fence = table.length();
        }

        private ParallelHashSetSpliterator(AtomicReferenceArray<Object> table, float fillRatio, int startIndex, int fence) {
            this.table = table;
            this.fillRatio = fillRatio;
            this.nextIndex = startIndex;
            this.fence = fence;
        }

        @Override
        public synchronized boolean tryAdvance(Consumer<? super T> action) {
            while (nextIndex < fence) {
                Object element = table.get(nextIndex);
                nextIndex++;
                if (element != null && element != TOMBSTONE) {
                    action.accept((T) element);
                    return true;
                }
            }
            return false;
        }

        @Override
        public synchronized Spliterator<T> trySplit() {
            int remaining = remaining();
            if (remaining < 2) {
                return null;
            }
            return splitAt(nextIndex + (remaining / 2));
        }

        private Spliterator<T> splitAt(int splitPoint) {
            ParallelHashSetSpliterator<T> next = new ParallelHashSetSpliterator<T>(table, fillRatio, splitPoint, fence);
            fence = splitPoint;
            return next;
        }

        @Override
        public long estimateSize() {
            return (int) (remaining() * fillRatio);
        }

        @Override
        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.NONNULL;
        }

        private int remaining() {
            return fence - nextIndex;
        }
    }
}
