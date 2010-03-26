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
 * Common SchemaAction superclass
 * @author icoloma
 *
 */
public abstract class AbstractDatastoreAction implements DatastoreAction {

	static final char PATH_SEPARATOR = '/'; 
	
	/** required informational field to log the action name */
	protected String id;
	
	/** queue name to execute the schema migration, defaults to "default" */
	protected String queueName;
	
	/** the number of entities to process by each invocation, default 150 */
	protected Integer batchSize;

	/** subtasks that will be executed in parallel after this instance */
	protected List<DatastoreAction> actions = Lists.newArrayList();
	
	/** the action prior to this instance, null for none */
	protected DatastoreAction parent;
	
	protected DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	protected Log log = LogFactory.getLog(getClass());
	
	protected AbstractDatastoreAction(String id) {
		if (id.indexOf(PATH_SEPARATOR) != -1) {
			throw new IllegalArgumentException("'/' is not allowed as part of id names");
		}
		this.id = id;
	}

	@Override
	public DatastoreAction withQueue(String queueName) {
		this.queueName = queueName;
		return this;
	}
	

	/**
	 * @param params the params of the current request
	 * @return the serialized cursor value, if any. 
	 */
	protected Cursor deserializeCursor(Map<String, String> params) {
		String serializedCursor = params.get(ActionParamNames.CURSOR);
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
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(batchSize == null? ActionLauncher.DEFAULT_BATCH_SIZE : batchSize);
		Cursor cursor = deserializeCursor(params);
		if (cursor != null) {
			fetchOptions = fetchOptions.cursor(cursor);
		}
		return fetchOptions;
	}
	
	/**
	 * Defer action execution to another subsequent request 
	 * @param cursor the cursor to continue at, null if none
	 */
	public void deferProceed(Cursor cursor, String uri, Map<String, String> params) {
		if (uri == null) {
			throw new IllegalArgumentException("uri must be specified");
		}
		
		// set all params
		Queue queue = queueName == null? QueueFactory.getDefaultQueue() : QueueFactory.getQueue(queueName);
		TaskOptions url = TaskOptions.Builder.url(uri); 
		url.param(ActionParamNames.ACTION, getPath());
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			if (!key.equals(ActionParamNames.ACTION) && !key.equals(ActionParamNames.CURSOR) && entry.getValue() != null) {
				url.param(key, entry.getValue());
			}
		}

		// add the cursor
		if (cursor != null) {
			String sc = cursor.toWebSafeString();
			url.param(ActionParamNames.CURSOR, sc);
			log.info("Deferring " + getPath() + " with cursor " + sc);
		} else {
			log.info("Deferring " + getPath());
		}

		
		queue.add(url);
	}

	/**
	 * Process nested actions
	 * Invoke this method after the action executed has been completed (no defer was necessary)
	 * @param uri
	 * @param params
	 */
	protected void doNestedActions(String uri, Map<String, String> params) {
		log.info("Action " + getPath() + " completed.");
		for (DatastoreAction action : actions) {
			action.deferProceed(null, uri, params);
		}
	}

	@Override
	public DatastoreAction add(DatastoreAction... actions) {
		for (DatastoreAction action : actions) {
			this.actions.add(action);
			((AbstractDatastoreAction)action).setParent(this);
		}
		return this;
	}
	
	public DatastoreAction withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * @return the nested path of this action. This path can be used to execute strictly this action when deferring work. 
	 */
	@Override
	public String getPath() {
		return parent == null? id : parent.getPath() + PATH_SEPARATOR + id;
	}

	public DatastoreAction getParent() {
		return parent;
	}

	public void setParent(DatastoreAction parentAction) {
		this.parent = parentAction;
	}

	@Override
	public List<DatastoreAction> getActions() {
		return actions;
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
