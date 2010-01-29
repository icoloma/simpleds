package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;

/**
 * Wrapper to make injection of {@link EntityManager} attributes easier using Spring. 
 * @author icoloma
 */
public class SpringEntityManagerFactory implements FactoryBean<EntityManager> {
	
	private EntityManagerFactory factory = new EntityManagerFactory();

	@Override
	public EntityManager getObject() throws Exception {
		return factory.initialize();
	}

	@Override
	public Class<EntityManager> getObjectType() {
		return EntityManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	@Autowired(required=false)
	public void setDatastoreService(DatastoreService datastoreService) {
		this.factory.setDatastoreService(datastoreService);
	}

	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.factory.setEnforceSchemaConstraints(enforceSchemaConstraints);
	}

	@Autowired
	public void setPersistenceMetadataRepository(PersistenceMetadataRepository persistenceMetadataRepository) {
		this.factory.setPersistenceMetadataRepository(persistenceMetadataRepository);
	}
	
}
