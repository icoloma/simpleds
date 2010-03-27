package org.simpleds.schema.task;

import java.util.Map;

import org.simpleds.schema.TaskParamNames;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
 * Delete all _active_ sessions at the time of invocation. 
 * To cleanup all _expired_ sessions better use the SessionCleanupServlet class
 * as explained 
 * <a href="http://groups.google.com/group/google-appengine-java/browse_thread/thread/4f0d9af1c633d39a">here</a>
 * 
 * @author icoloma
 */
public class DeleteSessionsTask extends DeleteTask {

	/** configured duration of the session in millis, default 24 hours */
	private long sessionDuration = 24L * 60 * 60 * 1000;
	
	public DeleteSessionsTask() {
		super("delete-sessions");
	}

	private DeleteSessionsTask(String id) {
		super(id);
	}

	@Override
	protected Query createQuery(Map<String, String> params) {
		// get the timestamp
		String sTimestamp = params.get(TaskParamNames.DELETE_SESSIONS_TIMESTAMP);
		long timestamp;
		if (sTimestamp == null) {
			timestamp = System.currentTimeMillis() + sessionDuration;
			params.put(TaskParamNames.DELETE_SESSIONS_TIMESTAMP, String.valueOf(timestamp));
		} else {
			timestamp = Long.valueOf(sTimestamp);
		}
		
		// create the query
		return new Query("_ah_SESSION").addFilter("_expires", FilterOperator.LESS_THAN_OR_EQUAL, timestamp);
	}
	
	public DeleteSessionsTask withSessionDuration(long sessionDuration) {
		this.sessionDuration = sessionDuration;
		return this;
	}
}
