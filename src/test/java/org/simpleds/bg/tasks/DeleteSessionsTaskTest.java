package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.BackgroundTask;
import org.simpleds.bg.TaskRequest;

import com.google.appengine.api.datastore.Entity;

public class DeleteSessionsTaskTest extends AbstractTaskTest {

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
		TaskRequest request = new TaskRequest("/", new HashMap<String, String>());
		assertEquals(2, task.proceed(request));
		assertEquals(1, task.proceed(request));
		assertEquals(0, task.proceed(request));
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
