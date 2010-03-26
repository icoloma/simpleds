package org.simpleds.schema.action;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.test.AbstractDatastoreTest;

public class ClearCacheActionTest extends AbstractDatastoreTest {

	private ClearCacheAction action;
	
	@Before
	public void initAction() {
		action = new ClearCacheAction();
	}
	
	@Test
	public void testAction() {
		memcache.put("bar", "baz");
		memcache.put("baz", "foo");
		assertEquals(2, action.proceed("/mock-uri", new HashMap<String, String>()));
		assertEquals(0, action.proceed("/mock-uri", new HashMap<String, String>()));
	}
	
}
