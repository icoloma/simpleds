package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.BackgroundTask;
import org.simpleds.bg.TaskLauncher;
import org.simpleds.bg.TasksServlet;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class TaskLauncherTest extends AbstractTaskTest {

	private TaskLauncher launcher;
	
	private MockHttpServletRequest request;
	
	@Before
	public void initRepository() {
		launcher = new TaskLauncher().add(
			new DeleteSessionsTask().withBatchSize(2).add(new ClearCacheTask())
		);
		long farFuture = System.currentTimeMillis() + 10 * 24L * 60 * 60 * 1000;
		// sessions that should not be removed
		createSession(farFuture);
		createSession(farFuture);
		
		// sessions that should be removed
		createSession(0);
		createSession(1);
		createSession(2);
		request = new MockHttpServletRequest("GET", "/mock-uri");
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

	@Test
	public void testLaunch() throws Exception {
		assertEquals(5, datastoreService.prepare(new Query("_ah_SESSION")).countEntities());
		
		// second execution, start deleting sessions
		launcher.launch("/", "delete-sessions", new HashMap<String, String>());
		assertTaskAndCursor("delete-sessions", true);
		
		// third execution, finish deleting sessions and defer clear-cache
		launcher.launch("/", "delete-sessions", new HashMap<String, String>());
		assertTaskAndCursor("delete-sessions/clear-cache", false);
		
		// fourth execution, clear cache
		launcher.launch("/", "delete-sessions/clear-cache", new HashMap<String, String>());
		assertQueueEmpty();
		assertEquals(2, datastoreService.prepare(new Query("_ah_SESSION")).countEntities());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalTaskName() throws Exception {
		launcher.launch("/", "xxx", new HashMap<String, String>());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingTaskName() throws Exception {
		launcher.launch("/", null, new HashMap<String, String>());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testExistingTaskID() throws Exception {
		launcher.add(new ClearCacheTask());
		launcher.add(new ClearCacheTask());
	}

	private void assertTaskAndCursor(String action, boolean cursor) throws Exception {
		Map<String, String> next = reset();
		assertEquals(action, next.get(TasksServlet.TASK_PARAM));
		if (cursor) {
			assertNotNull(next.get(BackgroundTask.CURSOR_PARAM));
		} else {
			assertNull(next.get(BackgroundTask.CURSOR_PARAM));
		}
	}
	
	protected Map<String, String> reset() throws UnsupportedEncodingException {
		Map<String, String> next = parseTaskBody();
		request.removeAllParameters();
		for (Map.Entry<String, String> entry : next.entrySet()) {
			request.setParameter(entry.getKey(), entry.getValue());
		}
		return next;
	}
	
}
