package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.exception.RequiredFieldException;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Dummy2;
import org.simpleds.testdb.Dummy3;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Sets;

public class IndexManagerTest extends AbstractDatastoreTest {

	private PersistenceMetadataRepository repository;
	
	private IndexManagerImpl indexManager;
	private EntityManagerImpl entityManager;

	private Dummy1 dummy;
	private Dummy1 friend1;
	
	@Before
	public void setup() {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		repository = factory.createRepository();
		indexManager = new IndexManagerImpl();
		indexManager.setRepository(repository);
		indexManager.setDatastoreService(DatastoreServiceFactory.getDatastoreService());
		
		entityManager = new EntityManagerImpl();
		entityManager.setRepository(repository);
		entityManager.setDatastoreService(DatastoreServiceFactory.getDatastoreService());
		
		dummy = createDummy();
		entityManager.put(dummy);
		assertTrue(indexManager.get(dummy.getKey(), "friends").isEmpty());
		friend1 = createDummy();
		entityManager.put(friend1);
		indexManager.put(dummy.getKey(), "friends", Sets.newHashSet(friend1.getKey()));
		assertTrue(indexManager.get(dummy.getKey(), "friends").contains(friend1.getKey()));
	}
	
	@Test
	public void testFind() throws Exception {
		IndexQuery query = indexManager.newQuery(Dummy1.class, "friends").equal(friend1.getKey());
		List<Dummy1> l = indexManager.find(query);
		assertEquals(1, l.size());
		assertEquals(dummy.getKey(), l.get(0).getKey());
		
		List<Key> l2 = indexManager.find(query.keysOnly());
		assertEquals(1, l2.size());
		assertEquals(dummy.getKey(), l2.get(0));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFindByUnmappedProperty() throws Exception {
		entityManager.find(new SimpleQuery(Dummy1.class).equal("xxx", "foo"));
	}
	
	private Dummy1 createDummy() {
		Dummy1 dummy = new Dummy1();
		dummy.setName("foo");
		dummy.setDate(new Date());
		return dummy;
	}
	
	private Dummy2 createDummy2() {
		Dummy2 dummy2 = new Dummy2();
		return dummy2;
	}
	
	private Dummy3 createDummy3() {
		Dummy3 dummy3 = new Dummy3();
		return dummy3;
	}
	
}
