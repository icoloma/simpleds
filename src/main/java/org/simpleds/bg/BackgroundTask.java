package org.simpleds.bg;


/**
 * Unit of work on the Datastore. 
 * Implementing classes must include:
 * <ul>
 * <li>
 * Idempotent: It is impossible to know in advance how many times a task will be 
 * invoked, so it must be capable of resuming work. Specifically, a previous execution
 * may have failed at any point.
 * <li>
 * Thread-safe and immutability: Tasks are stored in a static structure, which means 
 * that several threads may be executing tasks at the same time. A task should not be 
 * modified by the {@link TaskRequest} contents. 
 * </li>
 * <li>
 * Break up into smaller parts: tasks should try to complete in 30 seconds or less,
 * which often means deferring work. 
 * </li>
 * </ul>
 * @author icoloma
 *
 */
public interface BackgroundTask {
	
	/** the default batch size to use */
	public static int DEFAULT_BATCH_SIZE = 150;

	/**
	 * Executes this task. 
	 * @param request the task request
	 * @return the number of entities processed in this batch execution
	 */
	public long proceed(TaskRequest request);
	
	/**
	 * @return A unique identifier for this task 
	 */
	public String getId();

	/**
	 * Specifies the number of entities to process per execution, default 150
	 */
	public BackgroundTask withBatchSize(int batchSize);

	/**
	 * Set the queue name to use. If not specified, the default queue will be used.
	 * @param queueName the name of the queue to use
	 * @return this instance, for chaining
	 */
	BackgroundTask withQueue(String queueName);

	/**
	 * @return the Queue to be used by this task when deferring
	 */
	String getQueueName();

	/**
	 * @return the batch size to be used by this task
	 */
	Integer getBatchSize();
	
}
