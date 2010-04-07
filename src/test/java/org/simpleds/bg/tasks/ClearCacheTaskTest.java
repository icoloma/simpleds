package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.tasks.ClearCacheTask;

public class ClearCacheTaskTest extends AbstractTaskTest {

	private ClearCacheTask action;
	
	@Before
	public void initAction() {
		action = new ClearCacheTask();
	}
	
	@Test
	public void testAction() {
		assertEquals(0, memcache.getStatistics().getItemCount());
		memcache.put("bar", "baz");
		memcache.put("baz", "foo");
		long num = memcache.getStatistics().getItemCount();
		action.proceed("/mock-uri", new HashMap<String, String>());
		// the item in the cache is the result of cleaning the TaskStats data
		assertEquals(1, action.proceed("/mock-uri", new HashMap<String, String>()));
		assertEquals(3, memcache.getStatistics().getItemCount());
	}
	
}
