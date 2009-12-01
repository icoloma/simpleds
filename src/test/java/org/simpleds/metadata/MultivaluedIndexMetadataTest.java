package org.simpleds.metadata;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.IndexManagerFactory;
import org.simpleds.IndexManagerImpl;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;

public class MultivaluedIndexMetadataTest extends AbstractDatastoreTest {

	MultivaluedIndexMetadata metadata;
	
	private PersistenceMetadataRepository repository;
	
	@Before
	public void setup() {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		repository = factory.initialize();
		IndexManagerFactory indexManagerFactory = new IndexManagerFactory();
		indexManagerFactory.setDatastoreService(datastoreService);
		indexManagerFactory.setPersistenceMetadataRepository(factory.initialize());
		indexManagerFactory.initialize();
		IndexManagerImpl indexManager = (IndexManagerImpl) indexManagerFactory.getIndexManager();
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
