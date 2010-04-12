package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.TaskLauncher;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class TaskLauncherTest extends AbstractTaskTest {

	private static final String TASK_PARAM = "_task";
	
	private TaskLauncher launcher;
	
	@Before
	public void initLauncher() {
		launcher = new TaskLauncher().add(
			new DeleteSessionsTask().withBatchSize(2)
		);
		long farFuture = System.currentTimeMillis() + 10 * 24L * 60 * 60 * 1000;
		// sessions that should not be removed
		createSession(farFuture);
		createSession(farFuture);
		
		// sessions that should be removed
		createSession(0);
		createSession(1);
		createSession(2);
		request.setParameter(TASK_PARAM, "delete-sessions");
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
		
		// start deleting sessions
		launcher.launch(request);
		assertNotNull(request.getCursor());
		
		// finish deleting sessions 
		launcher.launch(request);
		assertEquals(2, datastoreService.prepare(new Query("_ah_SESSION")).countEntities());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalTaskName() throws Exception {
		request.setParameter(TASK_PARAM, "xxx");
		launcher.launch(request);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingTaskName() throws Exception {
		request.removeParameter(TASK_PARAM);
		launcher.launch(request);
	}
	
}
