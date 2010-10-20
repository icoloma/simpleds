package org.simpleds;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.simpleds.metadata.PersistenceMetadataRepository;
import org.springframework.beans.factory.FactoryBean;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Wrapper to make injection of {@link IndexManager} attributes easier using Spring.
 * @author icoloma
 */
@Singleton
public class SpringIndexManagerFactory implements FactoryBean<IndexManager> {

	@Inject
	private PersistenceMetadataRepository persistenceMetadataRepository;

	@Inject
	private DatastoreService datastoreService;

	private boolean enforceSchemaConstraints = true;

	@PostConstruct
	public void initialize() {
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		if (persistenceMetadataRepository == null) {
			throw new IllegalArgumentException("persistenceMetadataRepository cannot be null.");
		}
		IndexManagerImpl imi = new IndexManagerImpl();
		imi.setDatastoreService(datastoreService);
		imi.setPersistenceMetadataRepository(persistenceMetadataRepository);
		imi.setEnforceSchemaConstraints(enforceSchemaConstraints);
	}

	@Override
	public IndexManager getObject() {
		return IndexManagerFactory.getIndexManager();
	}

	@Override
	public Class<IndexManager> getObjectType() {
		return IndexManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setPersistenceMetadataRepository( PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

}
