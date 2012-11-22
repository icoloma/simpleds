package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

public class SimpleQueryTest extends AbstractEntityManagerTest {
	
    // TODO: add test withDeadline and withReadPolicy
	
	@Test
	public void testClone() throws Exception {
		SimpleQuery query = entityManager.createQuery(Dummy1.KIND);
		query.equal("name", "bar");
		query.equal("evalue", Dummy1.EnumValues.BAR);
		SimpleQuery copy = query.clone();
		assertNotSame(query.getFilterPredicates(), copy.getFilterPredicates());
		assertEquals(query.getFilterPredicates().size(), copy.getFilterPredicates().size());
		
		query.withLimit(10);
		copy = query.clone();
		assertNotSame(query.getFetchOptions(), copy.getFetchOptions());
		assertEquals(10, copy.getFetchOptions().getLimit().intValue());
		
	}
	
	@Test
	public void testIsNull() throws Exception {
		SimpleQuery query = entityManager.createQuery(Dummy1.class);
		query.equal("name", null);
		query.isNull("evalue");
		List<FilterPredicate> predicates = query.getFilterPredicates();
		assertEquals(1, predicates.size());
		assertEquals("evalue", predicates.get(0).getPropertyName());
		assertNull(predicates.get(0).getValue());
	}
	
	@Test
	public void testValidateSimpleQueryOK() throws Exception {
		SimpleQuery query = entityManager.createQuery(Dummy1.class);
		query.equal("date", new Date());
		query.equal("name", null);
		query.equal("int1", 2);
		query.equal("__key__", KeyFactory.createKey(Dummy1.KIND, 1));
		query.sortAsc("name");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateUnindexedProperty() throws Exception {
		SimpleQuery query = entityManager.createQuery(Dummy1.class);
		query.equal("int2", 2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateSimpleQueryWrongSortName() throws Exception {
		SimpleQuery query = entityManager.createQuery(Dummy1.class);
		query.sortAsc("xxx");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateSimpleQueryWrongPropertyClass() throws Exception {
		SimpleQuery query = entityManager.createQuery(Dummy1.class);
		query.equal("date", 1);
	}
	
	@Test
	public void testValidateSimpleQueryCollectionOK() throws Exception {
		addMetadata(CollectionDummy.class);
		SimpleQuery query = entityManager.createQuery(CollectionDummy.class);
		query.equal("intList", 1);
	}
	
	@Test(expected=ClassCastException.class)
	public void testValidateSimpleQueryCollectionWrongItemClass() throws Exception {
		addMetadata(CollectionDummy.class);
		SimpleQuery query = entityManager.createQuery(CollectionDummy.class);
		query.equal("intList", "foo");
	}
	
	@Test
	public void testIn() throws Exception {
		Date now = new Date();
		
		Dummy1 baz = Dummy1.create();
		baz.setOverridenNameDate(now);
		entityManager.put(baz);
		entityManager.put(Dummy1.create());
		SimpleQuery query = entityManager.createQuery(Dummy1.KIND).in("date", ImmutableList.of(now));
		List<Dummy1> list = entityManager.find(query);
		assertEquals(1, list.size());
		assertEquals(baz.getKey(), list.get(0).getKey());
	}
	
	@Test
	public void testPredicateList() throws Exception {
		entityManager.put(Dummy1.create());
		SimpleQuery query = entityManager.createQuery(Dummy1.class)
			.withPredicate(new Predicate<Dummy1>() {

				@Override
				public boolean apply(Dummy1 input) {
					return !"foo".equals(input.getName());
				}
				
			});
		List<Dummy1> list = entityManager.find(query);
		assertTrue(list.isEmpty());
	}
	
	@Test
	public void testPredicateIterator() throws Exception {
		for (int i = 1; i < 7; i++) {
			entityManager.put(Dummy1.create());
		}
		Predicate<Dummy1> predicate = new Predicate<Dummy1>() {
			
			@Override
			public boolean apply(Dummy1 input) {
				return input.getKey().getId() % 2 == 0;
			}
			
		};
		
		// retrieve first two results
		CursorIterator<Dummy1> it = entityManager.createQuery(Dummy1.class)
				.sortAsc("__key__")
				.withPredicate(predicate)
				.asIterator();
		assertTrue(it.hasNext());
		assertEquals(2, it.next().getKey().getId());
		assertEquals(4, it.next().getKey().getId());
		assertTrue(it.hasNext());
		
		// confirm the cursor position (WITHOUT PREDICATE)
		Cursor cursor = it.getCursor();
		CursorIterator<Dummy1> it2 = entityManager.createQuery(Dummy1.class)
				.sortAsc("__key__")
				.withStartCursor(cursor)
				.asIterator();
		assertTrue(it2.hasNext());
		assertEquals(6, it2.next().getKey().getId());
		
		// retrieve the last two results
		it = entityManager.createQuery(Dummy1.class)
				.sortAsc("__key__")
				.withPredicate(predicate)
				.withStartCursor(cursor)
				.asIterator();
		assertEquals(6, it.next().getKey().getId());
		assertFalse(it.hasNext());
		try {
			it.next();
			fail();
		} catch (NoSuchElementException e) {
		}
	}

	@Entity
	@SuppressWarnings("unused")
	public static class CollectionDummy {
		
		@Id 
		private Key key;
		
		private List<Integer> intList;
	}
}
