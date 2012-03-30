package org.simpleds.metadata;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.simpleds.converter.ConverterFactory;
import org.simpleds.testdb.Dummy1;
import org.zeroturnaround.javarebel.ClassEventListener;

public class ClassMetadataReloaderTest {

	private ClassMetadataReloader classMetadataReloader;
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	@Test
	public void test() {
		ClassMetadataFactory classMetadataFactory = new ClassMetadataFactory();
		classMetadataFactory.setConverterFactory(new ConverterFactory());
		persistenceMetadataRepository = new PersistenceMetadataRepository();
		persistenceMetadataRepository.setClassMetadataFactory(classMetadataFactory );
		persistenceMetadataRepository.add(Dummy1.class);
		classMetadataReloader = new ClassMetadataReloader();
		classMetadataReloader.setPersistenceMetadataRepository(persistenceMetadataRepository);
		ClassMetadata metadata = persistenceMetadataRepository.get(Dummy1.class);
		assertSame(metadata, persistenceMetadataRepository.get(Dummy1.class));
		
		// reload
		classMetadataReloader.onClassEvent(ClassEventListener.EVENT_RELOADED, Dummy1.class);
		assertNotSame(metadata, persistenceMetadataRepository.get(Dummy1.class));
	}
	
}
