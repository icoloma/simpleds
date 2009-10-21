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

public class EntityManagerTest extends AbstractDatastoreTest {

	private PersistenceMetadataRepository repository;
	
	private EntityManagerImpl entityManager;
	
	@Before
	public void setup() {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		repository = factory.createRepository();
		entityManager = new EntityManagerImpl();
		entityManager.setRepository(repository);
		entityManager.setDatastoreService(DatastoreServiceFactory.getDatastoreService());
	}
	
	@Test
	public void testSinglePutSuccess() {
		// root entity
		Dummy1 dummy = createDummy();
		entityManager.put(dummy);
		assertNotNull(dummy.getKey());
		
		// parent-child
		Dummy2 dummy2 = createDummy2();
		entityManager.put(dummy.getKey(), dummy2);
		assertEquals(dummy2.getKey(), entityManager.findChildrenKeys(dummy.getKey(), Dummy2.class).get(0));
	}
	
	@Test
	public void testSingleGet() {
		Dummy1 dummy = createDummy();
		entityManager.put(dummy);
		Dummy1 retrieved = entityManager.get(dummy.getKey());
		assertNotNull(retrieved);
		assertEquals(dummy.getKey(),  retrieved.getKey());
	}
	
	@Test
	@SuppressWarnings("cast")
	public void testMultiplePutWithKeysSuccess() {
		// root entities
		Key key = KeyFactory.createKey(Dummy1.class.getSimpleName(), 1);
		Dummy1 dummy1 = createDummy();
		dummy1.setKey(key);
		Dummy2 dummy2 = createDummy2();
		dummy2.setKey(KeyFactory.createKey(Dummy2.class.getSimpleName(), 1));
		Dummy3 dummy3 = createDummy3();
		dummy3.setKey(KeyFactory.createKey(Dummy3.class.getSimpleName(), 1));
		entityManager.put(Arrays.asList((Object)dummy1, (Object)dummy2, (Object)dummy3));
		
		// parent-childs
		entityManager.put(key, Arrays.asList(createDummy2(), createDummy2(), createDummy2()));
		assertEquals(3, entityManager.findChildrenKeys(key, Dummy2.class).size());
	}
	
	@Test
	@SuppressWarnings("cast")
	public void testMultiplePutWithoutKeysSuccess() {
		// root entities
		Dummy1 dummy1 = createDummy();
		Dummy2 dummy2 = createDummy2();
		Dummy3 dummy3 = createDummy3();
		dummy3.setKey(KeyFactory.createKey(Dummy3.class.getSimpleName(), 1));
		entityManager.put(Arrays.asList((Object)dummy1, (Object)dummy2, (Object)dummy3));
		assertNotNull(dummy1.getKey());
		assertNotNull(dummy2.getKey());
		assertNotNull(dummy3.getKey());
		
		// parent-childs
		List<Dummy2> children = Arrays.asList(createDummy2(), createDummy2(), createDummy2());
		entityManager.put(dummy1.getKey(), children);
		assertNotNull(children.get(0).getKey());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSinglePutMissingRequiredKey() {
		// dummy3 has not especified @GeneratedValue
		Dummy3 dummy3 = createDummy3();
		entityManager.put(dummy3);
	}
	
	@Test(expected=RequiredFieldException.class)
	public void testSinglePutMissingRequiredField() {
		Dummy1 dummy = new Dummy1();
		entityManager.put(dummy);
		fail("Persisted entity with missing required fields");
	}
	
	@Test
	public void testFind() {
		Dummy1  dummy = createDummy();
		entityManager.put(dummy);
		SimpleQuery query = new SimpleQuery(Dummy1.class).equal("name", "foo");
		List<Dummy1> result = entityManager.find(query);
		assertTrue(result.size() >= 1);
		assertEquals("foo", result.get(0).getName());
	}
	
	@Test
	public void testRelationIndex() throws Exception {
		Dummy1 dummy = createDummy();
		entityManager.put(dummy);
		assertTrue(entityManager.getRelationIndex(dummy.getKey(), "friends").isEmpty());
		Dummy1 friend1 = createDummy();
		entityManager.put(friend1);
		entityManager.setRelationIndex(dummy.getKey(), "friends", Sets.newHashSet(friend1.getKey()));
		assertTrue(entityManager.getRelationIndex(dummy.getKey(), "friends").contains(friend1.getKey()));
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
