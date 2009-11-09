package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;

/**
 * Factory class for easier Spring configuration
 * @author Nacho
 *
 */
public class SpringEntityManagerFactory implements FactoryBean, InitializingBean {

	/** delegate factory class */
	private EntityManagerFactory factory;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		factory.initializeEntityManager();
	}
	
	@Override
	public EntityManager getObject() throws Exception {
		return factory.getEntityManager();
	}

	@Override
	public Class getObjectType() {
		return EntityManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	@Autowired(required=false)
	public void setDatastoreService(DatastoreService datastoreService) {
		factory.setDatastoreService(datastoreService);
	}

	public void setEnforceSchemaConstraints(boolean checkSchemaConstraints) {
		factory.setEnforceSchemaConstraints(checkSchemaConstraints);
	}

	public void setLocations(String[] locations) {
		factory.setLocations(locations);
	}

	@Autowired(required=false)
	public void setRepositoryFactory( PersistenceMetadataRepositoryFactory repositoryFactory) {
		factory.setRepositoryFactory(repositoryFactory);
	}
}
