package org.simpleds.functions;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.testdb.Child;
import org.simpleds.testdb.Root;

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Collections2;

public class EntityParentFunctionTest extends AbstractEntityManagerTest {

	@Test
	public void testFunction() {
		Key rootKey1 = entityManager.put(new Root());
		Key rootKey2 = entityManager.put(new Root());
		entityManager.put(rootKey1, new Child());
		entityManager.put(rootKey1, new Child());
		entityManager.put(rootKey2, new Child());
		List<Child> children = entityManager.find(entityManager.createQuery(Child.class));
		Collection<Key> parentKeys = Collections2.transform(children, new EntityToParentKeyFunction(Child.class));
		Iterator<Key> it = parentKeys.iterator();
		assertEquals(rootKey1, it.next());
		assertEquals(rootKey1, it.next());
		assertEquals(rootKey2, it.next());
	}
	
}
