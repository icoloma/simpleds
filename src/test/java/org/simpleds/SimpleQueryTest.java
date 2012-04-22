package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.testdb.Dummy1;

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
	public void testPredicate() throws Exception {
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

	@Entity
	@SuppressWarnings("unused")
	public static class CollectionDummy {
		
		@Id 
		private Key key;
		
		private List<Integer> intList;
	}
}
