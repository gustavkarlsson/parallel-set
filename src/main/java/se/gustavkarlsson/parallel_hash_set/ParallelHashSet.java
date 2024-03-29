package se.gustavkarlsson.parallel_hash_set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ParallelHashSet<T> extends AbstractSet<T> {

    private static final int MAXIMUM_CAPACITY = 1 << 30; // Must be a power of two
    private static final int DEFAULT_CAPACITY = 8;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final Object TOMBSTONE = new Object();

    private final float loadFactor;

    private AtomicReferenceArray<Object> table;
    private int size = 0;

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
            final Object existing = table.get(index);
            if (existing == null || existing == TOMBSTONE) {
                // Free slot. Set element
                table.set(index, element);
                size++;
                return true;
            }
            if (element.equals(existing)) {
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
            final Object existing = table.get(index);
            if ((existing == null || existing == TOMBSTONE) && table.compareAndSet(index, null, element)) {
                // Slot was free and remained free during CAS
                return true;
            } else if (element.equals(existing)) {
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
            if (existing != TOMBSTONE && element.equals(existing)) {
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
            if (existing != TOMBSTONE && element.equals(existing)
                    && table.compareAndSet(index, existing, TOMBSTONE)) {
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
        AtomicReferenceArray<Object> table = this.table;
        int index = element.hashCode();
        while (true) {
            index &= table.length() - 1;
            final Object existing = table.get(index);
            if (existing == null) {
                // Free slot. Element could not be found
                return false;
            }
            if (existing != TOMBSTONE && element.equals(existing)) {
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

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T1> T1[] toArray(T1[] array) {
        T1[] newArray = array.length >= size() ? array : (T1[]) java.lang.reflect.Array.newInstance(array.getClass()
                .getComponentType(), size());
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
        long removed = parallelStreamIndexed().filter(e -> !collection.contains(e.reference))
                .peek(e -> table.set(e.index, TOMBSTONE)).count();
        size -= removed;
        return removed > 0;
    }

    private Stream<IndexedReference<T>> parallelStreamIndexed() {
        return StreamSupport.stream(new IndexedSpliterator<>(this), true);
    }

    private void resizeTableIfNeeded(int potentialElements) {
        float maxLoad = table.length() * loadFactor;
        int potentialLoad = size() + potentialElements;
        if (potentialLoad > maxLoad) {
            resizeTable(potentialLoad);
        }
    }

    private void resizeTable(int requiredCapacity) {
        Stream<T> stream = parallelStream(); // Stream of current table contents
        table = createTable(requiredCapacity); // Switch table
        stream.forEach(this::addConcurrent); // Add elements from stream to new table
    }

    private AtomicReferenceArray<Object> createTable(int requiredCapacity) {
	    if (requiredCapacity >= MAXIMUM_CAPACITY) {
		    throw new RuntimeException("Maximum capacity exceeded");
	    }
	    int newSize = calculateIdealTableSize(requiredCapacity);
	    if (newSize > MAXIMUM_CAPACITY) {
		    newSize = MAXIMUM_CAPACITY;
	    }
	    return new AtomicReferenceArray<>(newSize);
    }

    private int calculateIdealTableSize(int requiredCapacity) {
	    int idealSize = (int) Math.ceil(requiredCapacity / loadFactor);
	    int actualSize = 2;
	    while (actualSize < idealSize) {
		    actualSize *= 2;
	    }
        return actualSize;
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
        table = new AtomicReferenceArray<>(table.length());
        size = 0;
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
        int removed = (int) parallelStreamIndexed().filter(e -> filter.test(e.reference))
                .peek(e -> table.set(e.index, TOMBSTONE)).count();
        size -= removed;
        return removed > 0;
    }

    private static class ParallelHashSetIterator<T> implements Iterator<T> {

        private final AtomicReferenceArray<Object> table;

        private int nextIndex = 0;

        public ParallelHashSetIterator(ParallelHashSet<T> set) {
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

        @SuppressWarnings("unchecked")
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

    private static class IndexedSpliterator<T> implements Spliterator<IndexedReference<T>> {

        private final AtomicReferenceArray<Object> table;
        private final float fillRatio;

        private int nextIndex;
        private int fence;

        public IndexedSpliterator(ParallelHashSet<T> set) {
            this.table = set.table;
            this.fillRatio = (float) set.size() / table.length();
            this.nextIndex = 0;
            this.fence = table.length();
        }

        private IndexedSpliterator(AtomicReferenceArray<Object> table, float fillRatio, int startIndex, int fence) {
            this.table = table;
            this.fillRatio = fillRatio;
            this.nextIndex = startIndex;
            this.fence = fence;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean tryAdvance(Consumer<? super IndexedReference<T>> action) {
            while (nextIndex < fence) {
                Object element = table.get(nextIndex);
                nextIndex++;
                if (element != null && element != TOMBSTONE) {
                    action.accept(new IndexedReference<>(nextIndex - 1, (T) element));
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<IndexedReference<T>> trySplit() {
            int remaining = remaining();
            if (remaining < 2) {
                return null;
            }
            return splitAt(nextIndex + (remaining / 2));
        }

        private Spliterator<IndexedReference<T>> splitAt(int splitPoint) {
            IndexedSpliterator<T> next = new IndexedSpliterator<>(table, fillRatio, splitPoint, fence);
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

    private static class ParallelHashSetSpliterator<T> implements Spliterator<T> {

        private final Spliterator<IndexedReference<T>> wrapped;

        public ParallelHashSetSpliterator(ParallelHashSet<T> set) {
            this.wrapped = new IndexedSpliterator<>(set);
        }

        private ParallelHashSetSpliterator(Spliterator<IndexedReference<T>> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public synchronized boolean tryAdvance(Consumer<? super T> action) {
            return wrapped.tryAdvance(e -> action.accept(e.reference));
        }

        @Override
        public synchronized Spliterator<T> trySplit() {
            return new ParallelHashSetSpliterator<>(wrapped.trySplit());
        }

        @Override
        public long estimateSize() {
            return wrapped.estimateSize();
        }

        @Override
        public int characteristics() {
            return wrapped.characteristics();
        }
    }

    private static class IndexedReference<T> {

        private final int index;
        private final T reference;

        public IndexedReference(int index, T reference) {
            this.index = index;
            this.reference = reference;
        }
    }
}
