package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a {@link IndexManager} instance
 * @author icoloma
 */
public class IndexManagerFactory {

	private DatastoreService datastoreService;

	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private boolean enforceSchemaConstraints = true;
	
	private static IndexManager instance;
	
	public IndexManager initialize() {
		if (persistenceMetadataRepository == null) {
			persistenceMetadataRepository = PersistenceMetadataRepositoryFactory.getPersistenceMetadataRepository();
			if (persistenceMetadataRepository == null) {
				throw new IllegalArgumentException("persistenceMetadataRepository cannot be null.");
			}
		}
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		IndexManagerImpl imi = new IndexManagerImpl();
		imi.setDatastoreService(datastoreService);
		imi.setRepository(persistenceMetadataRepository);
		imi.setEnforceSchemaConstraints(enforceSchemaConstraints);
		instance = imi;
		return instance;
	}
	
	public static IndexManager getIndexManager() {
		return instance;
	}
	
	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setPersistenceMetadataRepository( PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

}
