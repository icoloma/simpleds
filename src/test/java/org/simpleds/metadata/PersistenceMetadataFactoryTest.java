package org.simpleds.metadata;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Dummy2;
import org.simpleds.testdb.Excluded;

public class PersistenceMetadataFactoryTest {

	@Test
	public void testRetrieveClasses() throws Exception {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		PersistenceMetadataRepository repository = factory.initialize();
		assertNotNull(repository.get(Dummy1.class));
		assertNotNull(repository.get(Dummy2.class));
		try {
			repository.get(Excluded.class);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}
	
}
