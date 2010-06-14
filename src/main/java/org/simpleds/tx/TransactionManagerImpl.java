package org.simpleds.tx;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;

public class TransactionManagerImpl implements TransactionManager {

	private DatastoreService datastoreService;
	
	private static Log log = LogFactory.getLog(TransactionManagerImpl.class);
	
	@Override
	public void commit() {
		commitOrRollback(true);
	}

	@Override
	public void rollback() {
		commitOrRollback(false);
	}
	
	private void commitOrRollback(boolean commit) {
		RuntimeException exception = null;
		Collection<Transaction> activeTransactions = datastoreService.getActiveTransactions();
		log.debug((commit? "Commit " : "Rollback ") + activeTransactions.size() + " transactions");
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
		log.debug((commit? "Commited " : "Rolled back ") + count + " transactions successfully");
		
		if (exception != null) {
			throw exception;
		}

	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

}
