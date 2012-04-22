package org.simpleds.metadata;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.simpleds.annotations.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.Reloader;
import org.zeroturnaround.javarebel.ReloaderFactory;

/**
 * Reloads the persistence metadata when a class change is triggered by JRebel.
 * This class will load/reload any class annotated as {@link Entity}.
 * To register, bind ClassMetadataReloader as an eager singleton.
 * @author icoloma
 */
@Singleton
public class ClassMetadataReloader implements ClassEventListener {

	@Inject
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private static Logger log = LoggerFactory.getLogger(ClassMetadataReloader.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public void onClassEvent(int eventType, Class klass) {
		if (klass.getAnnotation(Entity.class) != null) {
			log.info("Reloading SimpleDS metadata for " + klass.getName());
			persistenceMetadataRepository.remove(klass);
			persistenceMetadataRepository.add(klass);
		}
	}

	@Override
	public int priority() {
		return PRIORITY_DEFAULT;
	}

	public void setPersistenceMetadataRepository(PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
		Reloader reloaderFactory = ReloaderFactory.getInstance();
		reloaderFactory.addClassReloadListener(this);
		log.info("Class reloading for SimpleDS is enabled");
	}
	
}
