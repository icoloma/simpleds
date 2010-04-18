package org.simpleds;

import org.simpleds.cache.CacheManager;
import org.simpleds.cache.CacheManagerImpl;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * Creates a {@link EntityManager} instance. 
 * @author icoloma
 */
public class EntityManagerFactory extends AbstractDatastoreServiceAwareFactory {

	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private CacheManager cacheManager;
	
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
		if (cacheManager == null) {
			CacheManagerImpl cmi = new CacheManagerImpl();
			MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
			memcache.setNamespace(CacheManager.MEMCACHE_NAMESPACE);
			cmi.setMemcache(memcache);
			cmi.setPersistenceMetadataRepository(persistenceMetadataRepository);
			cacheManager = cmi;
		}
		EntityManagerImpl emi = new EntityManagerImpl();
		emi.setDatastoreService(datastoreService);
		emi.setCacheManager(cacheManager);
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

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}
