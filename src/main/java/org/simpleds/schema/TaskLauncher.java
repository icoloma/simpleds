package org.simpleds.schema;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A collection of {@link Task} to process.
 *  
 * @author icoloma
 *
 */
public class TaskLauncher {
	
	/** repository of all tasks to process */
	private Map<String, Task> tasks = Maps.newHashMap();
	
	/** list of root tasks */
	private List<Task> rootTasks = Lists.newArrayList();
	
	private static Log log = LogFactory.getLog(TaskLauncher.class);
	
	public TaskLauncher(Task... tasks) {
		this.add(tasks);
	}
	
	public TaskLauncher add(Task... tasks) {
		for (Task task : tasks) {
			rootTasks.add(task);
			this.tasks.put(task.getPath(), task);
			for (Task nested : task.getTasks()) {
				this.tasks.put(nested.getPath(), nested);
			}
		}
		log.info("Registered tasks: " + this.tasks.keySet());
		return this;
	}
	
	/**
	 * Launch all configured tasks in the configured uri
	 */
	public void launch(String uri) {
		for (Task task : rootTasks) {
			task.deferProceed(null, uri, new HashMap<String, String>());
		}
	}
	
	/**
	 * Launch the {@link Task} instance specified by the "task" request param.
	 * @param request the current request object
	 */
	public void proceed(HttpServletRequest request) {
		// extract the queueURL and the params
		Map<String, String> params = Maps.newHashMap();
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String name = e.nextElement();
			params.put(name, request.getParameter(name));
		}
		
		String uri = request.getRequestURI();
		int pos = uri.indexOf('?');
		if (pos != -1) {
			uri = uri.substring(0, pos);
		}
		
		// process it
		String taskId = params.get(TaskParamNames.TASK);
		if (taskId == null || taskId.length() == 0) {
			log.info("Task path is missing. Invoking launch() instead");
			launch(uri);
		} else {
			Task task = tasks.get(taskId);
			if (task == null) {
				throw new IllegalArgumentException("Could not find task: " + taskId);
			}
			task.proceed(uri, params);
		}
	}
	
	/**
	 * Return the current status of the launched tasks.
	 */
	public Collection<TaskStats> getStats() {
		return TaskStats.getTaskStats(tasks.keySet());
	}
	
	/**
	 * Set the queue to be used by all configured tasks. This value is ignored for tasks configured 
	 * explicitely using {@link Task}.withQueue()
	 * @param queueName the name of the queue to be used. 
	 * @return this instance for chaining
	 */
	public TaskLauncher withQueue(String queueName) {
		for (Task task : tasks.values()) {
			if (task.getQueueName() == null) { // set only if has not been set before
				task.withQueue(queueName);
			}
		}
		return this;
	}
	
	/**
	 * Set the queue to be used by all configured tasks. This call is ignored for tasks configured 
	 * explicitely using {@link Task}.withBatchSize()
	 * @param batchSize the number of entities to be process by each invocation
	 * @return this instance for chaining
	 */
	public TaskLauncher withBatchSize(int batchSize) {
		for (Task task : tasks.values()) {
			if (task.getBatchSize() == null) { // set only if has not been set before
				task.withBatchSize(batchSize);
			}
		}
		return this;
	}
	
}
