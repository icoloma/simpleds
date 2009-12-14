package org.simpleds.tx;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;

/**
 * Manages {@link Transaction} instances associated to this thread.
 * This class should be handled as a singleton object.
 * @author icoloma
 *
 */
public class TransactionManagerImpl implements TransactionManager {

	private DatastoreService datastoreService;
	
	private ThreadLocal<TransactionStatus> transactions = new ThreadLocal<TransactionStatus>() {
		@Override
		protected TransactionStatus initialValue() { return new TransactionStatus(); }
	};
	
	private static Log log = LogFactory.getLog(TransactionManagerImpl.class);
	
	@Override
	public void pushContext() {
		TransactionStatus status = getTransactionStatus();
		status.pushContext();
	}

	@Override
	public Transaction beginTransaction() {
		if (log.isDebugEnabled()) {
			log.debug("Creating new transaction for thread " + Thread.currentThread().getName());
		}
		Transaction transaction = datastoreService.beginTransaction();
		getTransactionStatus().add(transaction);
		return transaction;
	}

	public TransactionStatus getTransactionStatus() {
		return transactions.get();
	}
	
	@Override
	public List<Transaction> getActiveTransactions() {
		TransactionStatus status = getTransactionStatus();
		return status.getActiveTransactions();
	}

	@Override
	public void commit() {
		commitOrRollback(true);
	}

	@Override
	public void rollback() {
		commitOrRollback(false);
	}
	
	private void commitOrRollback(boolean commit) {
		TransactionStatus status = getTransactionStatus();
		int nestingLevel = status.popContext();
		if (nestingLevel == 0) {
			try {
				RuntimeException exception = null;
				List<Transaction> activeTransactions = getActiveTransactions();
				log.debug((commit? "Commit " : "Rollback ") + activeTransactions.size() + " for thread " + Thread.currentThread().getName());
				int count = 0;
				for (Transaction transaction : activeTransactions) {
					if (transaction.isActive()) {
						try {
							if (commit) {
								transaction.commit();
							} else {
								transaction.rollback();
							}
							count++;
						} catch (RuntimeException e) {
							if (exception == null) {
								exception = e;
							} else { // log any other subsequent exceptions
								log.debug(e, e);
							}
						}
					}
				}
				log.debug((commit? "Commited " : "Rolled back ") + count + " transactions successfully for thread " + Thread.currentThread().getName());
				
				if (exception != null) {
					throw exception;
				}
			} finally {
				transactions.remove();
			}
		} else if (nestingLevel < 0) {
			throw new IllegalStateException("Uneven " + (commit? "commit" : "rollback") + "() invoked. TransactionManager.pushContext() has not been invoked");
		}
	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

}
