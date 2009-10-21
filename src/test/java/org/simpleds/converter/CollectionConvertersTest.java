package org.simpleds.converter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;

import com.google.common.collect.Sets;

public class CollectionConvertersTest {

	@Test
	public void testListConverter() {
		Converter listConverter = ConverterFactory.getCollectionConverter(List.class, Integer.class);
		List l = Arrays.asList(1, 2, 3);
		List ds = (List) listConverter.javaToDatastore(l);
		assertEquals(3, ds.size());
		assertEquals(1L, ds.get(0));
		assertArrayEquals(l.toArray(), ((List)listConverter.datastoreToJava(ds)).toArray());
	}
	
	@Test
	public void testSetConverter() {
		Converter setConverter = ConverterFactory.getCollectionConverter(Set.class, String.class);
		Set js = Sets.newHashSet("a", "b", "c");
		Set ds = (Set) setConverter.javaToDatastore(js);
		assertEquals(3, ds.size());
		assertArrayEquals(js.toArray(), ((Set)setConverter.datastoreToJava(ds)).toArray());
	}
	
	@Test
	public void testNull() {
		assertConvertNull(List.class);
		assertConvertNull(Set.class);
		assertConvertNull(SortedSet.class);
	}

	private void assertConvertNull(Class<? extends Collection> collectionClass) {
		CollectionConverter converter = ConverterFactory.getCollectionConverter(collectionClass, Integer.class);
		assertNull(converter.javaToDatastore(null));
		assertTrue(((Collection)converter.datastoreToJava(null)).isEmpty());
	}
	
}
