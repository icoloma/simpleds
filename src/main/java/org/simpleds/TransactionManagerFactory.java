package org.simpleds;

import org.simpleds.tx.TransactionManager;
import org.simpleds.tx.TransactionManagerImpl;

/**
 * Creates a {@link TransactionManager} instance. 
 * @author icoloma
 */
public class TransactionManagerFactory extends DatastoreServiceAwareFactory {

	private static TransactionManager transactionManager;
	
	public TransactionManager initialize() {
		super.initDatastoreService();
		if (transactionManager == null) {
			transactionManager = new TransactionManagerImpl();
			((TransactionManagerImpl)transactionManager).setDatastoreService(datastoreService);
		}
		return transactionManager;
	}

	public static TransactionManager getTransactionManager() {
		return transactionManager;
	}

}
