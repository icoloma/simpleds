package org.simpleds.bg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * Common superclass
 * @author icoloma
 *
 */
public abstract class AbstractBackgroundTask implements BackgroundTask {

	/** the id of this task */
	protected String id;
	
	/** queue name to execute this task when deferring, defaults to "default" */
	protected String queueName;
	
	/** the number of entities to process by each invocation, default 150 */
	protected Integer batchSize;

	protected DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	protected Logger log = LoggerFactory.getLogger(getClass());
	
	protected AbstractBackgroundTask(String id) {
		this.id = id;
	}

	@Override
	public final long proceed(TaskRequest request) {
		if (batchSize == null) {
			batchSize = BackgroundTask.DEFAULT_BATCH_SIZE;
		}
		if (request.getCursor() == null) {
			TaskStats.start(this);
		}
		long numResults = doProceed(request);
		TaskStats.addResults(this, numResults);
		return numResults;
	}

	protected abstract long doProceed(TaskRequest request);
	
	@Override
	public BackgroundTask withQueue(String queueName) {
		this.queueName = queueName;
		return this;
	}
	
	/**
	 * Create fetch options from the servlet-provided parameters.
	 * The returned instance will apply the batchSize attribute limit of this instance 
	 * and the cursor provided by the request, if any. 
	 * @param request the current task request
	 */
	protected FetchOptions createFetchOptions(TaskRequest request) {
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(batchSize);
		Cursor cursor = request.getCursor();
		if (cursor != null) {
			fetchOptions = fetchOptions.cursor(cursor);
		}
		return fetchOptions;
	}
	
	/**
	 * Defer task execution to another subsequent request 
	 * @param request the current request object
	 */
	public void deferProceed(TaskRequest request) {
		log.info("Deferring " + getId() + " with cursor " + request.getCursor() == null? "<none>" : request.getCursor().toWebSafeString());
		TaskOptions url = TaskOptions.Builder.withUrl(request.getUri()); 
		for (String paramName : request.getParameterNames()) {
			url.param(paramName, request.getParameter(paramName));
		}
		getQueue().add(url);
	}
	
	protected Queue getQueue() {
		return queueName == null? QueueFactory.getDefaultQueue() : QueueFactory.getQueue(queueName);
	}

	/**
	 * Process nested tasks
	 * This should be invoked after task completion
	 */
	protected void notifyFinalization() {
		TaskStats.end(this);
	}
	
	public BackgroundTask withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * @return the nested path of this task. This path can be used to execute strictly this task when deferring work. 
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getQueueName() {
		return queueName;
	}

	@Override
	public Integer getBatchSize() {
		return batchSize;
	}


}
