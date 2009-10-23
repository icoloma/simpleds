package org.simpleds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;

/**
 * Transaction support is not yet operative, do not rely on this class yet.
 * @author icoloma
 *
 */
public class TransactionManager implements PlatformTransactionManager {

	@Autowired
	private DatastoreService service;
	
	private static Log log = LogFactory.getLog(TransactionManager.class);
	
	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		getRequiredTransaction().commit();
	}

	private Transaction getRequiredTransaction() {
		Transaction tx = service.getCurrentTransaction();
		if (tx == null) {
			throw new IllegalStateException("There is no active transaction");
		}
		return tx;
	}

	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition)
			throws TransactionException {
		boolean newTransaction = false;
		Transaction tx = service.getCurrentTransaction();
		if (tx == null) {
			tx = service.beginTransaction();
			newTransaction = true;
		}
		return new DefaultTransactionStatus(tx, newTransaction, 
				/* ??? */ newTransaction /* ??? */, false, log.isDebugEnabled(), null);
	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {
		getRequiredTransaction().rollback();
	}

}
