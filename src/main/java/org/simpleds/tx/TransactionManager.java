package org.simpleds.tx;

import com.google.appengine.api.datastore.DatastoreFailureException;

/**
 * Propagates the commit() or rollback() invocation to all active transactions.
 * @author Nacho
 */
public interface TransactionManager {
	
	/**
	 * Rollback transactions bound to this Thread. Only active 
	 * transactions will be processed, any transaction that has been 
	 * manually commited or rolled back will be skipped.
	 * @throws DatastoreFailureException if a datastore error occurs.
	 */
	public void rollback();
	
	/**
	 * Commit transactions bound to the current Thread. Only active 
	 * transactions will be processed, any transaction that has been 
	 * manually commited or rolled back will be skipped.
	 * @throws DatastoreFailureException - If a datastore error occurs.
	 */
	public void commit();

}
