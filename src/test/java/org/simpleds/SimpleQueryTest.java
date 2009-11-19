package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Query.FilterPredicate;

public class SimpleQueryTest extends AbstractDatastoreTest {

	@Test
	public void testClone() throws Exception {
		SimpleQuery query = new SimpleQuery("foo");
		query.equal("foo", "bar");
		query.equal("baz", Dummy1.EnumValues.BAR);
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
		SimpleQuery query = new SimpleQuery("foo");
		query.equal("foo", null);
		query.isNull("baz");
		List<FilterPredicate> predicates = query.getFilterPredicates();
		assertEquals(1, predicates.size());
		assertEquals("baz", predicates.get(0).getPropertyName());
		assertNull(predicates.get(0).getValue());
	}
	
}
