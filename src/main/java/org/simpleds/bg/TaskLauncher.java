package org.simpleds.bg;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A collection of {@link BackgroundTask} to process.
 *  
 * @author icoloma
 *
 */
public class TaskLauncher {
	
	private static final long serialVersionUID = 1L;

	/** repository of all tasks to process */
	private Map<String, BackgroundTask> tasks = Maps.newHashMap();
	
	/** list of root tasks */
	private List<BackgroundTask> rootTasks = Lists.newArrayList();
	
	private static Log log = LogFactory.getLog(TaskLauncher.class);
	
	public TaskLauncher(BackgroundTask... tasks) {
		this.add(tasks);
	}
	
	public TaskLauncher add(BackgroundTask... tasks) {
		for (BackgroundTask task : tasks) {
			if (this.tasks.containsKey(task.getPath())) {
				throw new IllegalArgumentException("Attempted to register two root tasks with the same id: " + task.getPath());
			}
			rootTasks.add(task);
			this.tasks.put(task.getPath(), task);
			for (BackgroundTask nested : task.getTasks()) {
				this.tasks.put(nested.getPath(), nested);
			}
		}
		log.info("Registered tasks: " + this.tasks.keySet());
		return this;
	}
	
	/**
	 * Return the current status of the launched tasks.
	 */
	public Collection<TaskStats> getStats() {
		return TaskStats.getTaskStats(tasks.keySet());
	}
	
	/**
	 * Set the queue to be used by all configured tasks. This value is ignored for tasks configured 
	 * explicitely using {@link BackgroundTask}.withQueue()
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
	 * Launch a specific task ID.
	 * @param uri the uri to use for enqueueing deferred tasks
	 * @param taskId the id of the task to launch. 
	 * @param params the params to pass to the task
	 */
	public long launch(String uri, String taskId, Map<String, String> params) {
		BackgroundTask task = tasks.get(taskId);
		if (task == null) {
			throw new IllegalArgumentException("Could not find task: " + taskId);
		}
		return task.proceed(uri, params);
	}
	
}
