package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.BackgroundTask;

import com.google.appengine.api.datastore.Entity;

public class DeleteSessionsTest extends AbstractTaskTest {

	private BackgroundTask task;
	
	@Before
	public void prepareTest() {
		task = new DeleteSessionsTask().withBatchSize(2);
		createSession(0);
		createSession(0);
		createSession(0);
	}
	
	@Test
	public void test() {
		assertEquals(2, task.proceed("/", new HashMap<String, String>()));
		assertEquals(1, task.proceed("/", new HashMap<String, String>()));
		assertEquals(0, task.proceed("/", new HashMap<String, String>()));
	}

	/**
	 * Mocks the behavior in AppEngine
	 * @param expires
	 */
	private void createSession(long expires) {
		Entity session = new Entity("_ah_SESSION");
		session.setProperty("_expires", expires);
		datastoreService.put(session);
		memcache.put(session.getKey(), session);
	}

}
