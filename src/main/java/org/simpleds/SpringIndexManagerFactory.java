package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;

/**
 * Creates a {@link IndexManager} instance
 * @author icoloma
 */
public class SpringIndexManagerFactory implements FactoryBean, InitializingBean {

	private IndexManagerFactory factory = new IndexManagerFactory();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		factory.initialize();
	}
	
	@Override
	public IndexManager getObject() {
		return IndexManagerFactory.getIndexManager();
	}
	
	@Override
	public Class getObjectType() {
		return IndexManager.class;
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

	@Autowired(required=false)
	public void setRepositoryFactory( PersistenceMetadataRepositoryFactory repositoryFactory) {
		factory.setRepositoryFactory(repositoryFactory);
	}
	
}
