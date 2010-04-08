package org.simpleds.bg.tasks;

import java.util.Date;

import org.simpleds.bg.TaskRequest;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
 * This task will delete all sessions up to the specified expiration timestamp. If no timestamp is specified, 
 * it will remove only expired sessions.
 * 
 * @author icoloma
 */
public class DeleteSessionsTask extends DeleteTask {
	
	/** the dafult ID of this task */
	public static final String DEFAULT_ID = "delete-sessions";
	
	/** 
	 * the timestamp parameter (in millis). 
	 * If specified, sessions with expiration date prior to this will be removed. The default is the timestamp of
	 * the first invocation. 
	 */
	public static final String TIMESTAMP_PARAM = "delete-sessions.timestamp";

	/**
	 * Creates a new instance with id=ClearCacheTask.DEFAULT_ID
	 */
	public DeleteSessionsTask() {
		super(DEFAULT_ID);
	}

	private DeleteSessionsTask(String id) {
		super(id);
	}

	@Override
	protected Query createQuery(TaskRequest request) {
		// get the timestamp
		String sTimestamp = request.getParameter(TIMESTAMP_PARAM);
		long timestamp;
		if (sTimestamp == null) {
			timestamp = System.currentTimeMillis();
			request.setParameter(TIMESTAMP_PARAM, String.valueOf(timestamp));
		} else {
			timestamp = Long.valueOf(sTimestamp);
		}
		log.info("Deleting sessions where _expired <= " + new Date(timestamp));
		
		// create the query
		return new Query("_ah_SESSION").addFilter("_expires", FilterOperator.LESS_THAN_OR_EQUAL, timestamp);
	}
	
}
