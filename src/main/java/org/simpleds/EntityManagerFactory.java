package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a default {@link EntityManager} implementation
 * @author icoloma
 */
public class EntityManagerFactory implements FactoryBean {

	@Autowired(required=false)
	private DatastoreService datastoreService;

	@Autowired(required=false)
	private PersistenceMetadataRepositoryFactory repositoryFactory;
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	@Override
	public EntityManager getObject() {
		if (repositoryFactory == null) {
			throw new IllegalArgumentException("repositoryFactory cannot be null. Either set repositoryFactory or the locations attribute");
		}
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		EntityManagerImpl instance = new EntityManagerImpl();
		instance.setDatastoreService(datastoreService);
		instance.setRepository(repositoryFactory.createRepository());
		instance.setEnforceSchemaConstraints(enforceSchemaConstraints);
		return instance;
	}
	
	@Override
	public Class getObjectType() {
		return EntityManager.class;
	}
	
	@Override
	public boolean isSingleton() {
		return true;
	}
	
	/**
	 * Set the list of locations to search for persistent classes
	 * @param locations a list of locations like "classpath*:com/acme/model/**"
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
