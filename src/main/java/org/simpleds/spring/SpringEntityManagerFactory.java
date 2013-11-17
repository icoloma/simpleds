package org.simpleds.spring;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;
import org.simpleds.EntityManagerImpl;
import org.simpleds.cache.CacheManager;
import org.simpleds.cache.CacheManagerImpl;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * Wrapper to make injection of {@link EntityManager} attributes easier using Spring. 
 * @author icoloma
 */
@Singleton
public class SpringEntityManagerFactory implements FactoryBean<EntityManager> {
	
	@Inject 
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	@Autowired(required=false)
	private CacheManager cacheManager;

	@Autowired(required=false)
	private DatastoreService datastoreService;

	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	@PostConstruct
	public void initialize() {
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
		if (persistenceMetadataRepository == null) {
			throw new IllegalArgumentException("persistenceMetadataRepository cannot be null");
		}
		if (cacheManager == null) {
			cacheManager = new CacheManagerImpl();
		}
		EntityManagerImpl emi = new EntityManagerImpl();
		emi.setDatastoreService(datastoreService);
		emi.setCacheManager(cacheManager);
		emi.setPersistenceMetadataRepository(persistenceMetadataRepository);
		emi.setEnforceSchemaConstraints(enforceSchemaConstraints);
	}
	
	@Override
	public EntityManager getObject() {
		return EntityManagerFactory.getEntityManager();
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
		this.datastoreService = datastoreService;
	}
	
	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setPersistenceMetadataRepository(PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}
