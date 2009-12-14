package org.simpleds;

import org.junit.Before;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.ClassMetadataFactory;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PersistenceMetadataRepositoryFactory;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.tx.TransactionManagerImpl;

public class AbstractEntityManagerTest extends AbstractDatastoreTest {
	
	protected PersistenceMetadataRepository repository;
	
	protected EntityManagerImpl entityManager;
	
	protected TransactionManagerImpl transactionManager;
	
	@Before
	public void setup() {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		repository = factory.initialize();
		transactionManager = new TransactionManagerImpl();
		transactionManager.setDatastoreService(datastoreService);
		
		entityManager = new EntityManagerImpl();
		entityManager.setRepository(repository);
		entityManager.setDatastoreService(datastoreService);
		entityManager.setTransactionManager(transactionManager);
	}
	
	/**
	 * Add a ClassMetadata to this entityManager
	 */
	protected ClassMetadata addMetadata(Class<?> clazz) {
		ClassMetadataFactory factory = new ClassMetadataFactory();
		ClassMetadata cm = factory.createMetadata(clazz);
		repository.add(cm);
		return cm;
	}
}
