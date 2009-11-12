package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a {@link IndexManager} instance
 * @author icoloma
 */
public class IndexManagerFactory {

	private DatastoreService datastoreService;

	private PersistenceMetadataRepositoryFactory repositoryFactory;
	
	private boolean enforceSchemaConstraints = true;
	
	private static IndexManager instance;
	
	public void initialize() {
		if (repositoryFactory == null) {
			throw new IllegalArgumentException("repositoryFactory cannot be null.");
		}
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		IndexManagerImpl imi = new IndexManagerImpl();
		imi.setDatastoreService(datastoreService);
		imi.setRepository(repositoryFactory.createRepository());
		imi.setEnforceSchemaConstraints(enforceSchemaConstraints);
		instance = imi;
	}
	
	public static IndexManager getIndexManager() {
		return instance;
	}
	
	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setRepositoryFactory(PersistenceMetadataRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

}
