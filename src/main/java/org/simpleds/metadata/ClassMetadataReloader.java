package org.simpleds.metadata;

import org.simpleds.annotations.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.Reloader;
import org.zeroturnaround.javarebel.ReloaderFactory;

/**
 * Reloads the persistence metadata when a class change is triggered by JRebel.
 * This class will load/reload any class annotated as {@link Entity}.
 * To register, invoke ClassMetadataReloader.register() once on application deployment.
 * @author icoloma
 */
public class ClassMetadataReloader implements ClassEventListener {

	private static Logger log = LoggerFactory.getLogger(ClassMetadataReloader.class);
	
	private ClassMetadataReloader() {}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onClassEvent(int eventType, Class klass) {
		if (klass.getAnnotation(Entity.class) != null || klass.getAnnotation(javax.persistence.Entity.class) != null) {
			log.info("Reloading SimpleDS metadata for " + klass.getName());
			PersistenceMetadataRepositoryFactory.getPersistenceMetadataRepository().add(klass);
		}
	}

	@Override
	public int priority() {
		return PRIORITY_DEFAULT;
	}

	public static final void register() {
		Reloader reloaderFactory = ReloaderFactory.getInstance();
		reloaderFactory.addClassReloadListener(new ClassMetadataReloader());
		log.info("Class reloading for SimpleDS is enabled");
	}
	
}
