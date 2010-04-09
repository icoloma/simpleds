package org.simpleds.bg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;

/**
 * Common superclass
 * @author icoloma
 *
 */
public abstract class AbstractBackgroundTask implements BackgroundTask {

	/** the id of this task */
	protected String id;
	
	/** queue name to execute the schema migration, defaults to "default" */
	protected String queueName;
	
	/** the number of entities to process by each invocation, default 150 */
	protected Integer batchSize;

	protected DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	protected Log log = LogFactory.getLog(getClass());
	
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
	 * Ćreate fetch options from the servlet-provided parameters.
	 * The returned instance will apply the batchSize attribute limit of this instance 
	 * and the cursor provided by the request parameter, if any. 
	 * @param params the parameters map received by the servlet
	 * @return 
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
	 * @param cursor the cursor to continue at, null if none
	 */
	public void deferProceed(TaskRequest request) {
		log.info("Deferring " + getId() + " with cursor " + request.getCursor() == null? "<none>" : request.getCursor().toWebSafeString());
		TaskOptions url = TaskOptions.Builder.url(request.getUri()); 
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
