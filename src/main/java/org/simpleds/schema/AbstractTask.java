package org.simpleds.schema;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.common.collect.Lists;

/**
 * Common superclass
 * @author icoloma
 *
 */
public abstract class AbstractTask implements Task {

	static final char PATH_SEPARATOR = '/'; 
	
	/** the id of this task */
	protected String id;
	
	/** queue name to execute the schema migration, defaults to "default" */
	protected String queueName;
	
	/** the number of entities to process by each invocation, default 150 */
	protected Integer batchSize;

	/** subtasks that will be executed in parallel after this instance */
	protected List<Task> tasks = Lists.newArrayList();
	
	/** the parent task, null if none */
	protected Task parent;
	
	protected DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	protected Log log = LogFactory.getLog(getClass());
	
	protected AbstractTask(String id) {
		if (id.indexOf(PATH_SEPARATOR) != -1) {
			throw new IllegalArgumentException("'/' is not allowed as part of id names");
		}
		this.id = id;
	}

	@Override
	public final long proceed(String uri, Map<String, String> params) {
		if (batchSize == null) {
			batchSize = Task.DEFAULT_BATCH_SIZE;
		}
		String path = getPath();
		TaskStats.start(this);
		long numResults = doProceed(uri, params);
		TaskStats.addResults(this, numResults);
		return numResults;
	}

	protected abstract long doProceed(String uri, Map<String, String> params);
	
	@Override
	public Task withQueue(String queueName) {
		this.queueName = queueName;
		return this;
	}
	

	/**
	 * @param params the params of the current request
	 * @return the serialized cursor value, if any. 
	 */
	protected Cursor deserializeCursor(Map<String, String> params) {
		String serializedCursor = params.get(TaskParamNames.CURSOR);
		return serializedCursor == null? null : Cursor.fromWebSafeString(serializedCursor);
	}
	
	/**
	 * Ćreate fetch options from the servlet-provided parameters.
	 * The returned instance will apply the batchSize attribute limit of this instance 
	 * and the cursor provided by the request parameter, if any. 
	 * @param params the parameters map received by the servlet
	 * @return 
	 */
	protected FetchOptions createFetchOptions(Map<String, String> params) {
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(batchSize);
		Cursor cursor = deserializeCursor(params);
		if (cursor != null) {
			fetchOptions = fetchOptions.cursor(cursor);
		}
		return fetchOptions;
	}
	
	/**
	 * Defer task execution to another subsequent request 
	 * @param cursor the cursor to continue at, null if none
	 */
	public void deferProceed(Cursor cursor, String uri, Map<String, String> params) {
		if (uri == null) {
			throw new IllegalArgumentException("uri must be specified");
		}
		
		// set all params
		Queue queue = queueName == null? QueueFactory.getDefaultQueue() : QueueFactory.getQueue(queueName);
		TaskOptions url = TaskOptions.Builder.url(uri); 
		url.param(TaskParamNames.TASK, getPath());
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			if (!key.equals(TaskParamNames.TASK) && !key.equals(TaskParamNames.CURSOR) && entry.getValue() != null) {
				url.param(key, entry.getValue());
			}
		}

		// add the cursor
		if (cursor != null) {
			String sc = cursor.toWebSafeString();
			url.param(TaskParamNames.CURSOR, sc);
			log.info("Deferring " + getPath() + " with cursor " + sc);
		} else {
			log.info("Deferring " + getPath());
		}

		
		queue.add(url);
	}

	/**
	 * Process nested tasks
	 * This should be invoked after task completion
	 */
	protected void doNestedTasks(String uri, Map<String, String> params) {
		TaskStats.end(this);
		for (Task task : tasks) {
			task.deferProceed(null, uri, params);
		}
	}

	@Override
	public Task add(Task... tasks) {
		for (Task task : tasks) {
			this.tasks.add(task);
			((AbstractTask)task).setParent(this);
		}
		return this;
	}
	
	public Task withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * @return the nested path of this task. This path can be used to execute strictly this task when deferring work. 
	 */
	@Override
	public String getPath() {
		return parent == null? id : parent.getPath() + PATH_SEPARATOR + id;
	}

	public Task getParent() {
		return parent;
	}

	public void setParent(Task parent) {
		this.parent = parent;
	}

	@Override
	public List<Task> getTasks() {
		return tasks;
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
