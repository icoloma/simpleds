package org.simpleds;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;

/**
 * Wrapper to make injection of {@link IndexManager} attributes easier using Spring.
 * @author icoloma
 */
public class SpringIndexManagerFactory implements FactoryBean {

	private IndexManagerFactory factory = new IndexManagerFactory();

	@Override
	public Object getObject() throws Exception {
		return factory.initialize();
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
