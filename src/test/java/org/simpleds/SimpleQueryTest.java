package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.simpleds.test.AbstractDatastoreTest;

public class SimpleQueryTest extends AbstractDatastoreTest {

	@Test
	public void testClone() throws Exception {
		SimpleQuery query = new SimpleQuery("foo");
		query.equal("foo", "bar");
		SimpleQuery copy = query.clone();
		assertNotSame(query.getFilterPredicates(), copy.getFilterPredicates());
		assertEquals(query.getFilterPredicates().size(), copy.getFilterPredicates().size());
		
		query.withLimit(10);
		copy = query.clone();
		assertNotSame(query.getFetchOptions(), copy.getFetchOptions());
		assertEquals(10, copy.getFetchOptions().getLimit().intValue());
		
	}
	
}
