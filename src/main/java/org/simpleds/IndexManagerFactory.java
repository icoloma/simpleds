package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

/**
 * Creates a {@link IndexManager} instance
 * @author icoloma
 */
public class IndexManagerFactory extends DatastoreServiceAwareFactory {

	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private boolean enforceSchemaConstraints = true;
	
	private static IndexManager instance;
	
	public IndexManager initialize() {
		super.initDatastoreService();
		if (persistenceMetadataRepository == null) {
			persistenceMetadataRepository = PersistenceMetadataRepositoryFactory.getPersistenceMetadataRepository();
			if (persistenceMetadataRepository == null) {
				throw new IllegalArgumentException("persistenceMetadataRepository cannot be null.");
			}
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
	
	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setPersistenceMetadataRepository( PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

}
