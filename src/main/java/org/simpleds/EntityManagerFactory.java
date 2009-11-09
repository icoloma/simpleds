package org.simpleds;

import javax.annotation.PostConstruct;

import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a {@link EntityManager} instance. 
 * @author icoloma
 */
public class EntityManagerFactory {

	private DatastoreService datastoreService;

	private PersistenceMetadataRepositoryFactory repositoryFactory;
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	private static EntityManager instance;
	
	@PostConstruct
	public void initializeEntityManager() {
		if (repositoryFactory == null) {
			throw new IllegalArgumentException("repositoryFactory cannot be null. Either set repositoryFactory or the locations attribute");
		}
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		EntityManagerImpl emi = new EntityManagerImpl();
		emi.setDatastoreService(datastoreService);
		emi.setRepository(repositoryFactory.createRepository());
		emi.setEnforceSchemaConstraints(enforceSchemaConstraints);
		instance = emi;
	}
	
	public static EntityManager getEntityManager() {
		return instance;
	}
	
	/**
	 * Set the list of locations to search for persistent classes
	 * @param locations a list of locations e.g. "classpath*:com/acme/model/**"
	 */
	public void setLocations(String[] locations) {
		if (this.repositoryFactory == null) {
			this.repositoryFactory = new PersistenceMetadataRepositoryFactory();
		}
		this.repositoryFactory.setLocations(locations);
	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setRepositoryFactory(PersistenceMetadataRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	public void setEnforceSchemaConstraints(boolean checkSchemaConstraints) {
		this.enforceSchemaConstraints = checkSchemaConstraints;
	}

}
