package org.simpleds.schema;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Cursor;

/**
 * Schema migration action. Implementing classes must provide the following features:
 * <ul>
 * <li>
 * Idempotence: It is impossible to know in advance how many times an action will be 
 * invoked, so it must be capable of resuming tasks. Specifically, a previous execution
 * may have failed at any point.
 * <li>
 * Immutability: When using an static schema action structure, several threads may be 
 * running actions at the same time. To be on the safe side of things, once configured 
 * actions should not be modified by data in any servlet request. 
 * </li>
 * <li>
 * Break up into smaller parts: actions should try to complete in 30 seconds or less,
 * which often means deferring work. 
 * </li>
 * </ul>
 * @author icoloma
 *
 */
public interface DatastoreAction {

	/**
	 * Executes this schema migration action. 
	 * @param uri The queue invocation uri 
	 * @return the number of processed entities
	 */
	public long proceed(String uri, Map<String, String> params);
	
	/**
	 * Push this action into the queue to execute later
	 * @param cursor The cursor to continue from, null  if none
	 * @param uri The queue invocation uri 
	 * @param params the request params to use for the next invocation
	 */
	public void deferProceed(Cursor cursor, String uri, Map<String, String> params);
	
	/**
	 * @return the list of slash-separated schema actions up to this instance. 
	 * This path will be used to execute strictly this action when deferring work. 
	 */
	public String getPath();

	/**
	 * @return the list of nested actions 
	 */
	public List<DatastoreAction> getActions();

	/**
	 * Specifies the number of entities to process per execution, default 150
	 */
	public DatastoreAction withBatchSize(int batchSize);

	/**
	 * Add nested action/s to this instance. The added actions will be executed after 
	 * this one has been completed, but only if no work has been deferred.
	 * @param actions the list of actions to add.
	 * @return this instance, for nested chaining.
	 */
	DatastoreAction add(DatastoreAction... actions);
	
}
