package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

/**
 * Creates a {@link EntityManager} instance. 
 * @author icoloma
 */
public class EntityManagerFactory extends AbstractDatastoreServiceAwareFactory {

	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	private static EntityManager instance;
	
	public EntityManager initialize() {
		super.initDatastoreService();
		if (persistenceMetadataRepository == null) {
			persistenceMetadataRepository = PersistenceMetadataRepositoryFactory.getPersistenceMetadataRepository();
			if (persistenceMetadataRepository == null) {
				throw new IllegalArgumentException("persistenceMetadataRepository cannot be null");
			}
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
	
	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setPersistenceMetadataRepository(PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

}
