package org.simpleds.tx;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Transaction;

/**
 * Creates transactions bound to the current thread. When the commit() or rollback()
 * method is invoked, the call is propagated to all open transactions.
 * @author Nacho
 *
 */
public interface TransactionManager {

	/**
	 * Creates a managed transaction and adds it to the list associated to 
	 * the current Thread
	 * @return the created transaction
	 */
	public Transaction beginTransaction();
	
	/**
	 * Rollback transactions bound to the current Thread. Only active 
	 * transactions will be processed, any transaction that has been 
	 * manually commited or rolled back will be skipped.
	 * @throws DatastoreFailureException - If a datastore error occurs.
	 */
	public void rollback();
	
	/**
	 * Commit transactions bound to the current Thread. Only active 
	 * transactions will be processed, any transaction that has been 
	 * manually commited or rolled back will be skipped.
	 * @throws DatastoreFailureException - If a datastore error occurs.
	 */
	public void commit();

	/**
	 * @return the list of active transactions managed by this {@link TransactionManager}
	 */
	List<Transaction> getActiveTransactions();

	/**
	 * Invoked to prepare the TransactionManager. This method must be invoked 
	 * at least once per thread, and must be matched by a commit() or rollback()
	 * invocation. Notice that failing to do so could result in memory leaks.
	 */
	void pushContext();

}
