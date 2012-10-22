package org.simpleds;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.JsonStored;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class JsonStoredTest extends AbstractEntityManagerTest {

	private JsonStored instance;
	
	@Before
	public void setup() {
		repository.add(JsonStored.class);
		instance = new JsonStored();
		instance.setList(ImmutableList.of(Dummy1.create()));
		instance.setSet(ImmutableSet.of("foo", "bar"));
		instance.setMap(ImmutableMap.of("foo", Dummy1.create()));
	}
	
	@Test
	public void testStoreAndRetrieve() {
		entityManager.put(instance);
		JsonStored instance2 = entityManager.get(instance.getKey());
		assertNotSame(instance, instance2);
		assertTrue(instance2.getList() instanceof ArrayList);
		assertTrue(instance2.getList().get(0) instanceof Dummy1);
		assertTrue(instance2.getSet() instanceof HashSet);
		assertTrue("foo".equals(instance2.getSet().iterator().next()));
		assertTrue(instance2.getMap() instanceof HashMap);
		assertTrue(instance2.getMap().values().iterator().next() instanceof Dummy1);
	}
	
}
