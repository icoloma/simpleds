package org.simpleds.metadata;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.IndexManagerImpl;
import org.simpleds.spring.SpringPersistenceMetadataRepositoryFactory;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;

public class MultivaluedIndexMetadataTest extends AbstractDatastoreTest {

	MultivaluedIndexMetadata metadata;
	
	private PersistenceMetadataRepository repository;
	
	@Before
	public void setup() {
		SpringPersistenceMetadataRepositoryFactory factory = new SpringPersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		factory.initialize();
		repository = factory.getObject();
		IndexManagerImpl indexManager = new IndexManagerImpl();
		indexManager.setDatastoreService(datastoreService);
		indexManager.setPersistenceMetadataRepository(repository);
		metadata = indexManager.getIndexMetadata(Dummy1.class, "friends");
	}
	
	@Test
	public void testValidateIndexValueOK() throws Exception {
		metadata.validateIndexValue(Dummy1.createDummyKey());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateIndexValueFail() throws Exception {
		metadata.validateIndexValue(1);
	}
	
}
