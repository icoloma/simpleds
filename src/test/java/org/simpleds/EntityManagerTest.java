package org.simpleds;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.simpleds.exception.RequiredFieldException;
import org.simpleds.functions.EntityToKeyFunction;
import org.simpleds.testdb.*;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class EntityManagerTest extends AbstractEntityManagerTest {

	@Test
	public void testSinglePutSuccess() {
		// root entity
		Dummy1 dummy = Dummy1.create();
		entityManager.put(dummy);
		assertNotNull(dummy.getKey());
		
		// parent-child
		Dummy2 dummy2 = createDummy2();
		entityManager.put(dummy.getKey(), dummy2);
		assertEquals(dummy2.getKey(), entityManager.findChildrenKeys(dummy.getKey(), Dummy2.class).get(0));
	}
	
	@Test
	public void testSingleGet() {
		Dummy1 dummy = Dummy1.create();
		entityManager.put(dummy);
		Dummy1 retrieved = entityManager.get(dummy.getKey());
		assertNotNull(retrieved);
		assertEquals(dummy.getKey(),  retrieved.getKey());
		assertEquals("foobar", dummy.getBigString());
	}
	
	@Test
	public void testEmptyGet() {
		assertEquals(ImmutableMap.of(), entityManager.get(Collections.EMPTY_LIST));
	}
	
	@Test
	public void testEmptyPut() {
		entityManager.put(Collections.EMPTY_LIST);
	}
	
	@Test
	public void testMultipleGet() {
		Dummy1 dummy1 = Dummy1.create();
		dummy1.setName("dummy1");
		Dummy1 dummy2 = Dummy1.create();
		dummy2.setName("dummy2");
		ImmutableList<Dummy1> dummies = ImmutableList.of(dummy1, dummy2);
		entityManager.put(dummies);
		
		Map<Key, Dummy1> retrieved = entityManager.get(Collections2.transform(dummies, new EntityToKeyFunction<Dummy1>(Dummy1.class)));
		assertEquals("dummy1", retrieved.get(dummy1.getKey()).getName());
		assertEquals("dummy2", retrieved.get(dummy2.getKey()).getName());
	}
	
	@Test
	public void testMixedGetAndPut() {
		Dummy1 dummy1 = Dummy1.create();
		Dummy3 dummy3 = new Dummy3();
		dummy3.setKey(KeyFactory2.createKey(Dummy3.class, 1));
		CacheableEntity cacheableEntity = CacheableEntity.create();
		BasicVersionedClass vc = new BasicVersionedClass();
		
		List l = ImmutableList.of(dummy3, dummy1, cacheableEntity, vc);
		entityManager.put(l);
		
		// modify the datastore value to check it is using the cached version
		cacheableEntity.setName("xxx");
		datastoreService.put(entityManager.javaToDatastore(cacheableEntity));
		assertEquals((Long) 0L, vc.getVersion());
		
		Collection keys = Collections2.transform(l, new EntityToKeyFunction());
		Map<Key, ?> retrieved = entityManager.get(keys);
		Dummy3 retrievedDummy3 = (Dummy3) retrieved.get(dummy3.getKey());
		Dummy1 retrievedDummy1 = (Dummy1) retrieved.get(dummy1.getKey());
		CacheableEntity retrievedCacheableEntity = (CacheableEntity) retrieved.get(cacheableEntity.getKey());
		BasicVersionedClass retrievedVC = (BasicVersionedClass) retrieved.get(vc.getKey());
		
		assertEquals(dummy3.getKey(), retrievedDummy3.getKey());
		assertEquals(dummy1.getKey(), retrievedDummy1.getKey());
		assertEquals(dummy1.getName(), retrievedDummy1.getName());
		assertEquals(dummy1.getBigString(), retrievedDummy1.getBigString());
		assertEquals("foo", retrievedCacheableEntity.getName());
		assertEquals(vc.getKey(), retrievedVC.getKey());
		assertEquals((Long) 0L, retrievedVC.getVersion());
		
		entityManager.put(l);
		retrieved = entityManager.get(keys);
		retrievedVC = (BasicVersionedClass) retrieved.get(vc.getKey());
		assertEquals((Long) 1L, retrievedVC.getVersion());
	}
	
	@Test
	public void testVersionedWithPK() {
		BasicVersionedClass vc = new BasicVersionedClass();
		vc.setKey(KeyFactory2.createKey(BasicVersionedClass.class, 10));
		List l = ImmutableList.of(vc);
		entityManager.put(l);
		assertEquals((Long) 0L, vc.getVersion());
		
		BasicVersionedClass vc2 = new BasicVersionedClass();
		vc.setKey(KeyFactory2.createKey(BasicVersionedClass.class, 11));
		entityManager.put(vc2);
		assertEquals((Long) 0L, vc2.getVersion());

	}
	
	@Test
	public void testMultipleGetFail() {
		entityManager.get(ImmutableList.of(KeyFactory2.createKey(Dummy1.class, 1)));
	}
	
	@Test
	@SuppressWarnings("cast")
	public void testMultiplePutWithKeysSuccess() {
		// root entities
		Key key = KeyFactory.createKey(Kinds.DUMMY1, 1);
		Dummy1 dummy1 = Dummy1.create();
		dummy1.setKey(key);
		Dummy1 dummy2 = Dummy1.create();
		dummy2.setKey(KeyFactory.createKey(Kinds.DUMMY1, 2));
		entityManager.put(Arrays.asList(dummy1, dummy2));
		
		// parent-childs
		entityManager.put(key, Arrays.asList(createDummy2(), createDummy2(), createDummy2()));
		assertEquals(3, entityManager.findChildrenKeys(key, Dummy2.class).size());
	}
	
	@Test
	@SuppressWarnings("cast")
	public void testMultiplePutWithoutKeysSuccess() {
		// root entities
		Dummy1 dummy1 = Dummy1.create();
		Dummy1 dummy2 = Dummy1.create();
		Dummy1 dummy3 = Dummy1.create();
		dummy3.setKey(Dummy1.createDummyKey());
		entityManager.put(Arrays.asList(dummy1, dummy2, dummy3));
		assertNotNull(dummy1.getKey());
		assertNotNull(dummy2.getKey());
		assertNotNull(dummy3.getKey());
		
		// parent-childs
		List<Dummy2> children = Arrays.asList(createDummy2(), createDummy2(), createDummy2());
		entityManager.put(dummy1.getKey(), children);
		assertNotNull(children.get(0).getKey());
	}
	
	@Test
	public void testPutParentFail() throws Exception {
		// root entity with a parent
		putShouldFail(Dummy1.createDummyKey(), new Root());
		
		// child entity with wrong parent
		putShouldFail(Dummy1.createDummyKey(), new Child());
		
		// child entity without parent
		putShouldFail(null, new Child());
	}
		
	@Test
	public void testGetParentFail() throws Exception {
		// same for searches
		findShouldFail(Dummy1.createDummyKey(), Root.class);
		findShouldFail(Dummy1.createDummyKey(), Child.class);
	}
	
	@Test
	public void testPutMissingRequiredKey() {
		// dummy3 has not especified @GeneratedValue
		Dummy3 dummy3 = createDummy3();
		putShouldFail(null, dummy3);
	}
	
	@Test
	public void testPutMissingRequiredFields() {
		Dummy1 dummy = new Dummy1();
		putShouldFail(null, dummy);
	}
	
	@Test
	public void testFind() {
		Dummy1  dummy = Dummy1.create();
		entityManager.put(dummy);
		SimpleQuery query = entityManager.createQuery(Dummy1.class).equal(Attrs.NAME, "foo");
		List<Dummy1> result = entityManager.find(query);
		assertTrue(result.size() >= 1);
		assertEquals("foo", result.get(0).getName());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFindByUnmappedProperty() throws Exception {
		entityManager.find(entityManager.createQuery(Dummy1.class).equal("xxx", "foo"));
	}
	
	@Test
	public void testValidateParentPass() throws Exception {
		Root  root = new Root();
		Child child = new Child();
		List<Child> childList = Arrays.asList(new Child(), new Child());
		List<Root> rootList = Arrays.asList(new Root(), new Root());
		
		// generate root keys
		entityManager.put(root);
		entityManager.put(rootList);
		
		// generate children keys
		entityManager.put(root.getKey(), child);
		entityManager.put(root.getKey(), childList);
		
		// pre-assigned children keys
		entityManager.put(child);
		entityManager.put(childList);
		
		// search
		entityManager.findChildren(root.getKey(), Child.class);
		entityManager.find(entityManager.createQuery(Root.class));
		entityManager.find(entityManager.createQuery(Child.class));
	}
	
	@Test
	public void testRefresh() {
		Dummy1 dummy = Dummy1.create();
		entityManager.put(dummy);
		Dummy1 dummy2 = new Dummy1();
		dummy2.setKey(dummy.getKey());
		entityManager.refresh(dummy2);
		assertEquals(dummy.getKey(), dummy2.getKey());
		assertEquals(dummy.getName(), dummy2.getName());
		assertEquals(dummy.getOverridenNameDate(), dummy2.getOverridenNameDate());
		assertEquals(dummy.getBigString(), dummy2.getBigString());
	}
	
	private void putShouldFail(Key parentKey, Object instance) {
		try {
			entityManager.put(parentKey, instance);
			fail("Put operation should fail");
		} catch (RequiredFieldException e) {
			out.println(e);
		} catch (IllegalArgumentException e) {
			out.println(e);
		}
		try {
			entityManager.put(parentKey, Arrays.asList(instance));
			fail("Put operation should fail");
		} catch (RequiredFieldException e) {
			out.println(e);
		} catch (IllegalArgumentException e) {
			out.println(e);
		}
	}
	
	private void findShouldFail(Key parentKey, Class clazz) {
		try {
			entityManager.find(entityManager.createQuery(parentKey, clazz));
			fail("find operation should fail");
		} catch (IllegalArgumentException e) {
			out.println(e);
		}
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
