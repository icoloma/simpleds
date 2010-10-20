package org.simpleds;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Lists;

public class PagedQueryTest extends AbstractEntityManagerTest {

	List<Dummy1> dummies; 
	
	public static int PAGE_SIZE = 3;
	
	@Before
	public void setup() {
		// clear the database
		List<Key> keys = entityManager.find(entityManager.createQuery(Dummy1.class).keysOnly());
		entityManager.delete(keys);
		
		// persist instances
		dummies = Lists.newArrayList();
		for (int i = 0; i < 29; i++) {
			dummies.add(Dummy1.create());
		}
		entityManager.put(dummies);
	}
	
	@Test
	public void testPaginate() throws Exception {
		PagedQuery query = entityManager.createPagedQuery(Dummy1.class).setPageSize(PAGE_SIZE);
		
		// count 
		PagedList<Dummy1> pagedList = query.asPagedList();
		assertEquals(29, pagedList.getTotalResults());
		
		// first page
		assertEquals(PAGE_SIZE, pagedList.getData().size());
		assertEquals(dummies.get(0).getKey(), pagedList.getData().get(0).getKey());
		
		// second page
		query.setPageIndex(1);
		pagedList = query.asPagedList();
		assertEquals(dummies.get(PAGE_SIZE).getKey(), pagedList.getData().get(0).getKey());
		
		// last page
		query.setPageIndex(9);
		pagedList = query.asPagedList();
		assertEquals(2, pagedList.getData().size());
		assertEquals(dummies.get(dummies.size() - 1).getKey(), pagedList.getData().get(1).getKey());
	}
	
}
