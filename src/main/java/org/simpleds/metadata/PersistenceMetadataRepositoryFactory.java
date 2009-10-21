package org.simpleds.metadata;

import java.io.IOException;

import javax.persistence.Entity;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class PersistenceMetadataRepositoryFactory {

	private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private ClassMetadataFactory classMetadataFactory = new ClassMetadataFactory();
	
	/** the list of ant-style package locations, such as classpath*:com/acme/model/** */
	private String[] locations;
	
	public PersistenceMetadataRepository createRepository() {
		try {
			if (locations == null) {
				throw new IllegalArgumentException("locations has not been specified");
			}
			PersistenceMetadataRepository repository = new PersistenceMetadataRepository();
			MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();
			for (String location : locations) {
				Resource[] resources = resolver.getResources(location);
				for (Resource resource : resources) {
					if (resource.isReadable()) {
						MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
						AnnotationMetadata am = metadataReader.getAnnotationMetadata();
						if (am.hasAnnotation(Entity.class.getName())) {
							Class clazz = ClassUtils.forName(am.getClassName());
							ClassMetadata metadata = classMetadataFactory.createMetadata(clazz);
							repository.add(metadata);
						}
					}
				}
			}
			return repository;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public ResourcePatternResolver getResolver() {
		return resolver;
	}

	public void setResolver(ResourcePatternResolver resolver) {
		this.resolver = resolver;
	}

	public String[] getLocations() {
		return locations;
	}

	public void setLocations(String[] locations) {
		this.locations = locations;
	}

	public void setClassMetadataFactory(ClassMetadataFactory classMetadataFactory) {
		this.classMetadataFactory = classMetadataFactory;
	}

	
}
