package org.simpleds.schema;

public interface DatastoreParamNames {

	/** the id of the action to execute, leave empty for all */
	public static final String ACTION = "action";
	
	/** the cursor value, if any */
	public static final String CURSOR = "cursor";
	
	/** the timestamp when deleting sessions */
	public static final String DELETE_SESSIONS_TIMESTAMP = "delete-sessions.timestamp";
	
}
