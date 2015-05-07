package se.gustavkarlsson.parallel_hash_set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class ParallelHashSetTest {

	private static final int OTHER_SET_SIZE = 100;

	private static Set<Object> superSet;
	private static Set<Object> subSet;

	private ParallelHashSet<Object> set;

	@BeforeClass
	public static void setUpClass() throws Exception {
		superSet = setUpOtherSet();
		subSet = setUpSubSet(superSet);
	}

	private static Set<Object> setUpOtherSet() {
		Set<Object> otherSet = new HashSet<>(OTHER_SET_SIZE);
		Random random = new Random(0);
		while (otherSet.size() < OTHER_SET_SIZE) {
			otherSet.add(new BigInteger(130, random).toString(32));
		}
		return Collections.unmodifiableSet(otherSet);
	}

	private static Set<Object> setUpSubSet(Set<Object> otherSet) {
		Set<Object> subSet = new HashSet<>(OTHER_SET_SIZE / 2);
		boolean add = true;
		for (Object element : otherSet) {
			if (add) {
				subSet.add(element);
			}
			add = !add;
		}
		return subSet;
	}

	@Before
	public void setUp() throws Exception {
		set = new ParallelHashSet<>();
	}

	@Test
	public void testCreateWithInitialCapacity() throws Exception {
		new ParallelHashSet<>(10);
	}

	@Test
	public void testCreateWithZeroInitialCapacity() throws Exception {
		new ParallelHashSet<>(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNegativeInitialCapacityFails() throws Exception {
		new ParallelHashSet<>(-1);
	}

	@Test
	public void testCreateWithLoadFactor() throws Exception {
		new ParallelHashSet<>(0.5f);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithZeroLoadFactorFails() throws Exception {
		new ParallelHashSet<>(0f);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNegativeLoadFactorFails() throws Exception {
		new ParallelHashSet<>(-0.1f);
	}

	@Test
	public void testAddSingleElement() throws Exception {
		assertTrue(set.add("test"));
		assertEquals(1, set.size());
	}

	@Test(expected = NullPointerException.class)
	public void testAddNullElementFails() throws Exception {
		set.add(null);
	}

	@Test
	public void testAddSameObject() throws Exception {
		String element = "test";
		set.add(element);
		assertFalse(set.add(element));
		assertEquals(1, set.size());
	}

	@Test
	public void testAddEqualObject() throws Exception {
		set.add("test");
		assertFalse(set.add("test"));
		assertEquals(1, set.size());
	}

	@Test
	public void testAddOtherObject() throws Exception {
		set.add("test");
		assertTrue(set.add("other"));
		assertEquals(2, set.size());
	}

	@Test
	public void testRemoveSingleElement() throws Exception {
		String element = "test";
		set.add(element);
		assertTrue(set.remove(element));
		assertTrue(set.isEmpty());
	}

	@Test
	public void testRemoveNullElement() throws Exception {
		assertFalse(set.remove(null));
	}

	@Test
	public void testRemoveNonExistingElement() throws Exception {
		assertFalse(set.remove("test"));
	}

	@Test
	public void testContainsSingleElement() throws Exception {
		String element = "test";
		set.add(element);
		assertTrue(set.contains(element));
	}

	@Test
	public void testContainsSimilarElement() throws Exception {
		set.add("test");
		assertTrue(set.contains("test"));
	}

	@Test
	public void testToArray() throws Exception {
		set.addAll(superSet);
		Object[] array = set.toArray();
		assertEquals(set.size(), array.length);
		assertTrue(new HashSet<>(Arrays.asList(array)).equals(set));
	}

	@Test
	public void testToArrayEmpty() throws Exception {
		Object[] array = set.toArray();
		assertEquals(0, array.length);
	}

	@Test
	public void testContainsAll() throws Exception {
		set.addAll(superSet);
		assertTrue(set.containsAll(subSet));
	}

	@Test
	public void testContainsAllFailsIfItDoesNotContainAll() throws Exception {
		set.addAll(subSet);
		assertFalse(set.containsAll(superSet));
	}

	@Test(expected = NullPointerException.class)
	public void testContainsAllNullFails() throws Exception {
		set.containsAll(null);
	}

	@Test
	public void testAddAll() throws Exception {
		set.addAll(superSet);
		assertTrue(superSet.equals(set));
	}

	@Test
	public void testAddAllReturnsTrueOnChange() throws Exception {
		assertTrue(set.addAll(superSet));
	}

	@Test
	public void testAddAllReturnsFalseOnNoChange() throws Exception {
		set.addAll(superSet);
		assertFalse(set.addAll(superSet));
	}

	@Test(expected = NullPointerException.class)
	public void testAddAllNullFails() throws Exception {
		set.addAll(null);
	}

	@Test
	public void testRemoveAll() throws Exception {
		Set<Object> reliableSet = new HashSet<>();

		set.addAll(superSet);
		set.removeAll(subSet);

		reliableSet.addAll(superSet);
		reliableSet.removeAll(subSet);

		assertTrue(reliableSet.equals(set));
	}

	@Test
	public void testRemoveAllReturnsTrueOnChange() throws Exception {
		set.addAll(superSet);
		assertTrue(set.removeAll(subSet));
	}

	@Test
	public void testRemoveAllReturnsFalseOnNoChange() throws Exception {
		set.addAll(superSet);
		set.removeAll(subSet);
		assertFalse(set.removeAll(subSet));
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveAllNullFails() throws Exception {
		set.removeAll(null);
	}

	@Test
	public void testRemoveIfOneHit() throws Exception {
		set.add("test1");
		set.add("test2");
		assertTrue(set.removeIf("test1"::equals));
		assertEquals(1, set.size());
	}

	@Test
	public void testRemoveIfMiss() throws Exception {
		set.add("test");
		assertFalse(set.removeIf("test2"::equals));
		assertEquals(1, set.size());
	}

	@Test
	public void testRemoveIfNullFails() throws Exception {
		set.removeIf(null);
	}

	@Test
	public void testRetainAllSubSet() throws Exception {
		set.addAll(superSet);
		set.retainAll(subSet);
		assertTrue(subSet.equals(set));
	}

	@Test(expected = NullPointerException.class)
	public void testRetainAllNull() throws Exception {
		set.retainAll(null);
	}

	@Test
	public void testSizeEmpty() throws Exception {
		assertEquals(0, set.size());
	}

	@Test
	public void testSizeOne() throws Exception {
		set.add("test");
		assertEquals(1, set.size());
	}

	@Test
	public void testSizeDuplicate() throws Exception {
		set.add("test");
		set.add("test");
		assertEquals(1, set.size());
	}

	@Test
	public void testSizeMany() throws Exception {
		set.addAll(superSet);
		assertEquals(superSet.size(), set.size());
	}

	@Test
	public void testIsEmptyNoElements() throws Exception {
		assertTrue(set.isEmpty());
	}


	@Test
	public void testIsEmptyOneElement() throws Exception {
		set.add("test");
		assertFalse(set.isEmpty());
	}

	@Test
	public void testClearEmpty() throws Exception {
		set.clear();
		assertTrue(set.isEmpty());
	}

	@Test
	public void testClearWithElements() throws Exception {
		set.addAll(superSet);
		set.clear();
		assertTrue(set.isEmpty());
	}

	@Test
	public void testIteratorIteratesAll() throws Exception {
		Set<Object> verifySet = new HashSet<>();
		set.addAll(superSet);
		set.forEach(e -> assertTrue(verifySet.add(e)));
		assertTrue(verifySet.equals(set));
	}

	@Test
	public void testSpliteratorIteratesAll() throws Exception {
		Set<Object> verifySet = new HashSet<>();
		set.addAll(superSet);
		Spliterator<Object> split = set.spliterator();
		split.forEachRemaining(new Consumer<Object>() {
			@Override
			public void accept(Object element) {
				verifySet.add(element);
			}
		});
		assertTrue(verifySet.equals(set));
	}

	@Test
	public void testSpliteratorEstimateSize() throws Exception {
		set.addAll(superSet);
		assertEquals(superSet.size(), set.spliterator().estimateSize());
	}
}