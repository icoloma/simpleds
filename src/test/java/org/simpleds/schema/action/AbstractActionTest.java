package org.simpleds.schema.action;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.simpleds.test.AbstractDatastoreTest;

import com.google.appengine.api.labs.taskqueue.dev.QueueStateInfo;
import com.google.appengine.api.labs.taskqueue.dev.QueueStateInfo.TaskStateInfo;
import com.google.appengine.repackaged.com.google.common.collect.Maps;

public abstract class AbstractActionTest extends AbstractDatastoreTest {

	public void assertQueueEmpty() {
		QueueStateInfo qsi = queue.getQueueStateInfo().get("default");
		assertEquals(0, qsi.getTaskInfo().size());
	}
	
	/**
	 * Parses the first entry in the queue and removes it
	 */
	protected Map<String, String> parseTaskBody() {
		try {
			QueueStateInfo qsi = queue.getQueueStateInfo().get("default");
			assertEquals(1, qsi.getTaskInfo().size());
			TaskStateInfo task = qsi.getTaskInfo().get(0);
			String body = task.getBody();
			Map<String, String> result = Maps.newHashMap();
			for (String part : body.split("&")) {
				String[] s = part.split("=");
				result.put(s[0], URLDecoder.decode(s[1], "UTF-8"));
			}
			clearQueue();
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException();
		}
	}
	
	protected void clearQueue() {
		QueueStateInfo qsi = queue.getQueueStateInfo().get("default");
		for (TaskStateInfo task: qsi.getTaskInfo()) {
			queue.deleteTask("default", task.getTaskName());
		}
		assertEquals(0, queue.getQueueStateInfo().get("default").getCountTasks());
	}
	

}
