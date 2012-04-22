package org.simpleds.spring;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.spring.SpringPersistenceMetadataRepositoryFactory;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Dummy2;
import org.simpleds.testdb.Excluded;

public class SpringPersistenceMetadataRepositoryTest {

	@Test
	public void testRetrieveClasses() throws Exception {
		SpringPersistenceMetadataRepositoryFactory factory = new SpringPersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		factory.initialize();
		PersistenceMetadataRepository repository = factory.getObject();
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
