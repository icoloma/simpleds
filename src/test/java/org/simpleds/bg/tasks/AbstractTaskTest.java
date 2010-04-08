package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.bg.TaskRequest;

import com.google.appengine.api.labs.taskqueue.dev.QueueStateInfo;
import com.google.appengine.api.labs.taskqueue.dev.QueueStateInfo.TaskStateInfo;

public abstract class AbstractTaskTest extends AbstractEntityManagerTest {

	protected TaskRequest request;
	
	@Before
	public void initTaskRequest() {
		request = new TaskRequest("/", new HashMap<String, String>());
	}
	
	@After
	public void cleanQueue() {
		QueueStateInfo qsi = queue.getQueueStateInfo().get("default");
		for (TaskStateInfo task: qsi.getTaskInfo()) {
			queue.deleteTask("default", task.getTaskName());
		}
		// assertEquals(0, queue.getQueueStateInfo().get("default").getCountTasks());
	}
	
	/**
	 * Checks that there are n entries in the queue
	 */
	public void assertQueueEntries(int entriesCount) {
		QueueStateInfo qsi = queue.getQueueStateInfo().get("default");
		assertEquals(entriesCount, qsi.getTaskInfo().size());
	}
	
}
