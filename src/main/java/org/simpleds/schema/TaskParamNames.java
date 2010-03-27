package org.simpleds.schema;

public interface TaskParamNames {

	/** the id of the task to execute, leave empty for all */
	public static final String TASK = "task";
	
	/** the cursor value, if any */
	public static final String CURSOR = "cursor";
	
	/** the timestamp when deleting sessions */
	public static final String DELETE_SESSIONS_TIMESTAMP = "delete-sessions.timestamp";
	
}
