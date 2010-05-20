package org.simpleds;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.tx.TransactionManagerImpl;

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
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		repository = factory.initialize();
		transactionManager = new TransactionManagerImpl();
		transactionManager.setDatastoreService(datastoreService);
		
		EntityManagerFactory emFactory = new EntityManagerFactory();
		emFactory.setPersistenceMetadataRepository(repository);
		emFactory.setDatastoreService(datastoreService);
		entityManager = (EntityManagerImpl) emFactory.initialize();
	}
	
	/**
	 * Add a ClassMetadata to this entityManager
	 */
	protected ClassMetadata addMetadata(Class<?> clazz) {
		return repository.add(clazz);
	}
}
