package org.simpleds.schema;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Cursor;

/**
 * Unit of work on the Datastore. 
 * Implementing classes must take the following into consideration:
 * <ul>
 * <li>
 * Idempotence: It is impossible to know in advance how many times a task will be 
 * invoked, so it must be capable of resuming work. Specifically, a previous execution
 * may have failed at any point.
 * <li>
 * Immutability: When using an static structure, several threads may be 
 * executing tasks at the same time. To be on the safe side of things, once configured 
 * a task should not be modified by data in any servlet request. 
 * </li>
 * <li>
 * Break up into smaller parts: tasks should try to complete in 30 seconds or less,
 * which often means deferring work. 
 * </li>
 * </ul>
 * @author icoloma
 *
 */
public interface Task {
	
	/** the default batch size to use */
	public static int DEFAULT_BATCH_SIZE = 150;

	/**
	 * Executes this task. 
	 * @param uri The queue invocation uri 
	 * @return the number of processed entities
	 */
	public long proceed(String uri, Map<String, String> params);
	
	/**
	 * Push this task into the queue to execute later
	 * @param cursor The cursor to continue from, null  if none
	 * @param uri The queue invocation uri 
	 * @param params the request params to use for the next invocation
	 */
	public void deferProceed(Cursor cursor, String uri, Map<String, String> params);
	
	/**
	 * @return a slash-separated list of task ids, including all the parents of this instance. 
	 * This path will be used to execute strictly this task when deferring. 
	 */
	public String getPath();

	/**
	 * @return the list of nested tasks 
	 */
	public List<Task> getTasks();

	/**
	 * Specifies the number of entities to process per execution, default 150
	 */
	public Task withBatchSize(int batchSize);

	/**
	 * Add nested task/s to this instance. The added tasks will be executed after 
	 * this one has been completed, but only if no work has been deferred.
	 * @param tasks the list of tasks to add.
	 * @return this instance, for chaining.
	 */
	Task add(Task... tasks);

	/**
	 * Set the queue name to use. If not specified, the default queue will be used.
	 * @param queueName the name of the queue to use
	 * @return this instance, for chaining
	 */
	Task withQueue(String queueName);

	/**
	 * @return the Queue to be used by this task when deferring
	 */
	String getQueueName();

	/**
	 * @return the batch size to be used by this task
	 */
	Integer getBatchSize();
	
}