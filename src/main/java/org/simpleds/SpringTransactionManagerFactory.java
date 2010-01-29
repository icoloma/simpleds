package org.simpleds;


import org.simpleds.tx.TransactionManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;

/**
 * Wrapper to make injection of {@link TransactionManager} attributes easier using Spring. 
 * @author icoloma
 */
public class SpringTransactionManagerFactory implements FactoryBean<TransactionManager> {
	
	private TransactionManagerFactory factory = new TransactionManagerFactory();

	@Override
	public TransactionManager getObject() throws Exception {
		return factory.initialize();
	}

	@Override
	public Class<TransactionManager> getObjectType() {
		return TransactionManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	@Autowired(required=false)
	public void setDatastoreService(DatastoreService datastoreService) {
		this.factory.setDatastoreService(datastoreService);
	}

}
