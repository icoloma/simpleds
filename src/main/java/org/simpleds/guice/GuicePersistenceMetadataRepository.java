package org.simpleds.guice;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.simpleds.metadata.PersistenceMetadataRepository;

@Singleton
public class GuicePersistenceMetadataRepository extends PersistenceMetadataRepository {

	@Inject
	public void setPersistentClasses(@PersistentClasses Set<Class<?>> persistentClasses) {
		for (Class<?> clazz : persistentClasses) {
			add(clazz);
		}
	}
	
}
