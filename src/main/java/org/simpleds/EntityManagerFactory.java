package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.simpleds.tx.TransactionManager;
import org.simpleds.tx.TransactionManagerImpl;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a {@link EntityManager} instance. 
 * @author icoloma
 */
public class EntityManagerFactory {

	private DatastoreService datastoreService;

	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private TransactionManager transactionManager;
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	private static EntityManager instance;
	
	public EntityManager initialize() {
		if (persistenceMetadataRepository == null) {
			persistenceMetadataRepository = PersistenceMetadataRepositoryFactory.getPersistenceMetadataRepository();
			if (persistenceMetadataRepository == null) {
				throw new IllegalArgumentException("persistenceMetadataRepository cannot be null");
			}
		}
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		if (transactionManager == null) {
			transactionManager = new TransactionManagerImpl();
			((TransactionManagerImpl)transactionManager).setDatastoreService(datastoreService);
		}
		EntityManagerImpl emi = new EntityManagerImpl();
		emi.setDatastoreService(datastoreService);
		emi.setRepository(persistenceMetadataRepository);
		emi.setEnforceSchemaConstraints(enforceSchemaConstraints);
		emi.setTransactionManager(transactionManager);
		instance = emi;
		return instance;
	}
	
	public static EntityManager getEntityManager() {
		return instance;
	}
	
	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setPersistenceMetadataRepository(PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

}
