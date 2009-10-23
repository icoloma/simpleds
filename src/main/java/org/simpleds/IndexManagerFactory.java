package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Creates a {@link IndexManager} instance
 * @author icoloma
 */
public class IndexManagerFactory implements FactoryBean {

	@Autowired(required=false)
	private DatastoreService datastoreService;

	@Autowired(required=false)
	private PersistenceMetadataRepositoryFactory repositoryFactory;
	
	@Override
	public IndexManager getObject() {
		if (repositoryFactory == null) {
			throw new IllegalArgumentException("repositoryFactory cannot be null.");
		}
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		IndexManagerImpl instance = new IndexManagerImpl();
		instance.setDatastoreService(datastoreService);
		instance.setRepository(repositoryFactory.createRepository());
		return instance;
	}
	
	@Override
	public Class getObjectType() {
		return IndexManager.class;
	}
	
	@Override
	public boolean isSingleton() {
		return true;
	}
	
	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setRepositoryFactory(PersistenceMetadataRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

}
