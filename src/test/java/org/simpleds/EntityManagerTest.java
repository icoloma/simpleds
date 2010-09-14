package org.simpleds;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.simpleds.exception.RequiredFieldException;
import org.simpleds.functions.EntityToKeyFunction;
import org.simpleds.testdb.Child;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Dummy2;
import org.simpleds.testdb.Dummy3;
import org.simpleds.testdb.Root;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

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
	public void testMultipleGet() {
		Dummy1 dummy1 = Dummy1.create();
		Dummy1 dummy2 = Dummy1.create();
		ImmutableList<Dummy1> dummies = ImmutableList.of(dummy1, dummy2);
		entityManager.put(dummies);
		
		// same order
		List<Dummy1> retrieved = entityManager.get(Collections2.transform(dummies, new EntityToKeyFunction<Dummy1>(Dummy1.class)));
		assertEquals(dummies.get(0).getKey(), retrieved.get(0).getKey());
		assertEquals(dummies.get(1).getKey(), retrieved.get(1).getKey());
		
		// reverse order
		dummies = ImmutableList.of(dummy2, dummy1);
		retrieved = entityManager.get(Collections2.transform(dummies, new EntityToKeyFunction<Dummy1>(Dummy1.class)));
		assertEquals(dummies.get(0).getKey(), retrieved.get(0).getKey());
		assertEquals(dummies.get(1).getKey(), retrieved.get(1).getKey());
	}
	
	@Test
	public void testMultipleGetFail() {
		entityManager.get(ImmutableList.of(KeyFactory2.createKey(Dummy1.class, 1)));
	}
	
	@Test
	@SuppressWarnings("cast")
	public void testMultiplePutWithKeysSuccess() {
		// root entities
		Key key = KeyFactory.createKey(Dummy1.class.getSimpleName(), 1);
		Dummy1 dummy1 = Dummy1.create();
		dummy1.setKey(key);
		Dummy1 dummy2 = Dummy1.create();
		dummy2.setKey(KeyFactory.createKey(Dummy1.class.getSimpleName(), 2));
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
		Dummy1  dummy = Dummy1.create();
		entityManager.put(dummy);
		SimpleQuery query = entityManager.createQuery(Dummy1.class).equal("name", "foo");
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
	public void testValidateParentFail() throws Exception {
		// root entity with a parent
		putShouldFail(Dummy1.createDummyKey(), new Root());
		
		// child entity with wrong parent
		putShouldFail(Dummy1.createDummyKey(), new Child());
		
		// child entity without parent
		putShouldFail(null, new Child());
		
		// same for searches
		findShouldFail(Dummy1.createDummyKey(), Root.class);
		findShouldFail(Dummy1.createDummyKey(), Child.class);
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
		} catch (IllegalArgumentException e) {
			out.println(e);
		}
		try {
			entityManager.put(parentKey, Arrays.asList(instance));
			fail("Put operation should fail");
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
