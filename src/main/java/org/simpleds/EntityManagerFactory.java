package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a {@link EntityManager} instance. 
 * @author icoloma
 */
public class EntityManagerFactory {

	private DatastoreService datastoreService;

	private PersistenceMetadataRepository persistenceMetadataRepository;
	
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
		EntityManagerImpl emi = new EntityManagerImpl();
		emi.setDatastoreService(datastoreService);
		emi.setRepository(persistenceMetadataRepository);
		emi.setEnforceSchemaConstraints(enforceSchemaConstraints);
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

}
