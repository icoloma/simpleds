package org.simpleds.tx;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.Lists;

/**
 * Manages {@link Transaction} instances associated to this thread.
 * This class should be handled as a singleton object.
 * @author icoloma
 *
 */
public class TransactionManagerImpl implements TransactionManager {

	private DatastoreService datastoreService;
	
	private ThreadLocal<List<Transaction>> transactionsMap = new ThreadLocal<List<Transaction>>() {
		
		@Override
		protected java.util.List<Transaction> initialValue() {
			return Lists.newArrayList();
		}
		
	};
	
	private static Log log = LogFactory.getLog(TransactionManagerImpl.class);
	
	@Override
	public Transaction beginTransaction() {
		if (log.isDebugEnabled()) {
			log.debug("Creating new transaction for thread " + Thread.currentThread().getName());
		}
		Transaction transaction = datastoreService.beginTransaction();
		getOpenTransactions().add(transaction);
		return transaction;
	}

	@Override
	public void commit() {
		try {
			RuntimeException exception = null;
			for (Transaction transaction : getOpenTransactions()) {
				if (transaction.isActive()) {
					try {
						transaction.commit();
					} catch (RuntimeException e) {
						if (exception == null) {
							exception = e;
						} else { // log any other subsequent exceptions
							log.debug(e, e);
						}
					}
				}
			}
			if (exception != null) {
				throw exception;
			}
		} finally {
			transactionsMap.remove();
		}
	}

	@Override
	public List<Transaction> getOpenTransactions() {
		return transactionsMap.get();
	}

	@Override
	public void rollback() {
		try {
			RuntimeException exception = null;
			for (Transaction transaction : getOpenTransactions()) {
				if (transaction.isActive()) {
					try {
						transaction.rollback();
					} catch (RuntimeException e) {
						if (exception == null) {
							exception = e;
						} else { // log any other subsequent exceptions
							log.debug(e, e);
						}
					}
				}
			}
			if (exception != null) {
				throw exception;
			}
		} finally {
			transactionsMap.remove();
		}
	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

}
