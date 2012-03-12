package org.simpleds;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.simpleds.cache.CacheManager;
import org.simpleds.cache.CacheManagerImpl;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.CacheableEntity;
import org.simpleds.testdb.Child;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Dummy2;
import org.simpleds.testdb.Dummy3;
import org.simpleds.testdb.Root;
import org.simpleds.testdb.BasicVersionedClass;
import org.simpleds.tx.TransactionManagerImpl;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class AbstractEntityManagerTest extends AbstractDatastoreTest {
	
	protected PersistenceMetadataRepository repository;
	
	protected EntityManagerImpl entityManager;
	
	protected TransactionManagerImpl transactionManager;
	
	@After
	public void assertAllClosed() {
		assertTrue(datastoreService.getActiveTransactions().isEmpty());
	}

	@Before
	public void setupEntityManager() {
		repository = new PersistenceMetadataRepository();
		repository.add(CacheableEntity.class);
		repository.add(Child.class);
		repository.add(Dummy1.class);
		repository.add(Dummy2.class);
		repository.add(Dummy3.class);
		repository.add(Root.class);
		repository.add(BasicVersionedClass.class);
		
		transactionManager = new TransactionManagerImpl();
		transactionManager.setDatastoreService(datastoreService);
		
		CacheManagerImpl cmi = new CacheManagerImpl();
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService(CacheManager.MEMCACHE_NAMESPACE);
		cmi.setMemcache(memcache);
		cmi.setPersistenceMetadataRepository(repository);

		entityManager = new EntityManagerImpl();
		entityManager.setCacheManager(cmi);
		entityManager.setPersistenceMetadataRepository(repository);
		entityManager.setDatastoreService(datastoreService);
		EntityManagerFactory.setEntityManager(entityManager);
	}
	
	/**
	 * Add a ClassMetadata to this entityManager
	 */
	protected ClassMetadata addMetadata(Class<?> clazz) {
		return repository.add(clazz);
	}
}
