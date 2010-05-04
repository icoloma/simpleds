package org.simpleds.bg;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

/**
 * A collection of {@link BackgroundTask} to process.
 *  
 * @author icoloma
 *
 */
public class TaskLauncher {
	
	private static final long serialVersionUID = 1L;

	/** list of registered tasks */
	private Map<String, BackgroundTask> tasks = Maps.newHashMap();
	
	private static Log log = LogFactory.getLog(TaskLauncher.class);
	
	public TaskLauncher(BackgroundTask... tasks) {
		if (tasks != null && tasks.length > 0) {
			this.add(tasks);
		}
	}

	/**
	 * Add a series of tasks
	 */
	public TaskLauncher add(BackgroundTask... tasks) {
		for (BackgroundTask task : tasks) {
			this.tasks.put(task.getId(), task);
		}
		log.info("Registered tasks: " + this.tasks.keySet());
		return this;
	}
	
	/**
	 * Remove a task by its ID
	 */
	public TaskLauncher remove(String taskID) {
		tasks.remove(taskID);
		return this;
	}
	
	/**
	 * Return the current status of the launched tasks.
	 */
	public Collection<TaskStats> getStats() {
		return TaskStats.getTaskStats(tasks.keySet());
	}
	
	/**
	 * Set the queue to be used by all configured tasks. This value is ignored for 
	 * tasks configured explicitely using {@link BackgroundTask}.withQueue()
	 * @param queueName the name of the queue to be used. 
	 * @return this instance for chaining
	 */
	public TaskLauncher withQueue(String queueName) {
		for (BackgroundTask task : tasks.values()) {
			if (task.getQueueName() == null) { // set only if has not been set before
				task.withQueue(queueName);
			}
		}
		return this;
	}
	
	/**
	 * Set the queue to be used by all configured tasks. This call is ignored for tasks configured 
	 * explicitely using {@link BackgroundTask}.withBatchSize()
	 * @param batchSize the number of entities to be process by each invocation
	 * @return this instance for chaining
	 */
	public TaskLauncher withBatchSize(int batchSize) {
		for (BackgroundTask task : tasks.values()) {
			if (task.getBatchSize() == null) { // set only if it has not been set by hand
				task.withBatchSize(batchSize);
			}
		}
		return this;
	}

	/**
	 * Launch a specific task
	 * @param request the TaskRequest to execute
	 */
	public long launch(TaskRequest request) {
		BackgroundTask task = tasks.get(request.getTaskId());
		if (task == null) {
			throw new IllegalArgumentException("Could not find task: " + request.getTaskId());
		}
		return task.proceed(request);
	}
	
}
