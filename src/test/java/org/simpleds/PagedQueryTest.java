package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class PagedQueryTest extends AbstractDatastoreTest {

	private EntityManager entityManager;
	
	List<Dummy1> dummies; 
	
	private static int PAGE_SIZE = 3;
	
	@Before
	public void setup() {
		EntityManagerFactory factory = new EntityManagerFactory();
		PersistenceMetadataRepositoryFactory pmrFactory = new PersistenceMetadataRepositoryFactory();
		pmrFactory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		factory.setPersistenceMetadataRepository(pmrFactory.initialize());
		entityManager = factory.initialize();
		
		// clear the database
		List<Key> keys = entityManager.find(entityManager.createQuery(Dummy1.class).keysOnly());
		entityManager.delete(keys);
		
		// persist instances
		dummies = Lists.newArrayList();
		for (int i = 0; i < 29; i++) {
			dummies.add(createDummy(i));
		}
		entityManager.put(dummies);
	}
	
	@Test
	public void testPaginate() throws Exception {
		PagedQuery query = entityManager.createPagedQuery(Dummy1.class).setPageSize(PAGE_SIZE);
		
		// count 
		assertEquals(29, entityManager.count(query));
		
		// first page
		List<Dummy1> result = entityManager.find(query);
		assertEquals(PAGE_SIZE, result.size());
		assertEquals(dummies.get(0).getKey(), result.get(0).getKey());
		
		// second page
		query.setPageIndex(1);
		result = entityManager.find(query);
		assertEquals(dummies.get(PAGE_SIZE).getKey(), result.get(0).getKey());
		
		// last page
		query.setPageIndex(9);
		result = entityManager.find(query);
		assertEquals(2, result.size());
		assertEquals(dummies.get(dummies.size() - 1).getKey(), result.get(1).getKey());
	}
	
	@Test
	public void testPagedList() throws Exception {
		PagedQuery query = entityManager.createPagedQuery(Dummy1.class).setPageSize(PAGE_SIZE);
		query.setPageIndex(2);
		PagedList<Dummy1> list = entityManager.findPaged(query);
		assertEquals(29, list.getTotalResults());
		assertEquals(10, list.getTotalPages());
		
		PagedList<String> names = list.transform(new GetDummy1Name());
		assertTrue(names.getData().get(0) instanceof String);
	}
	
	private Dummy1 createDummy(int index) {
		Dummy1 dummy = new Dummy1();
		dummy.setName("foo" + index);
		dummy.setOverridenNameDate(new Date());
		return dummy;
	}
	
	private class GetDummy1Name implements Function<Dummy1, String> {

		@Override
		public String apply(Dummy1 from) {
			return from.getName();
		}
		
	}
	
}
