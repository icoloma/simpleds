package org.simpleds.metadata;

import java.io.IOException;
import java.util.Set;

import javax.persistence.Entity;

import org.simpleds.annotations.Id;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Sets;

public class PersistenceMetadataRepositoryFactory {

	private static final Class<?>[] ROOT_ANCESTORS = new Class<?>[] {};
	
	private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private ClassMetadataFactory classMetadataFactory = new ClassMetadataFactory();
	
	/** the list of ant-style package locations, such as classpath*:com/acme/model/** */
	private String[] locations;
	
	private static PersistenceMetadataRepository instance;
	
	@SuppressWarnings("deprecation")
	public PersistenceMetadataRepository initialize() {
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
						if (am.hasAnnotation(Entity.class.getName()) || 
								am.hasAnnotation(org.simpleds.annotations.Entity.class.getName())) {
							Class<?> clazz = ClassUtils.forName(am.getClassName(), ClassUtils.getDefaultClassLoader());
							ClassMetadata metadata = classMetadataFactory.createMetadata(clazz);
							repository.add(metadata);
						}
					}
				}
			}
			
			// assign expected parent kinds
			for (ClassMetadata metadata: repository.getAll()) {
				org.simpleds.annotations.Entity entity = metadata.getPersistentClass().getAnnotation(org.simpleds.annotations.Entity.class);
				Id idAnn = metadata.getKeyProperty().getAnnotation(Id.class);
				Class<?>[] cparents = idAnn != null? idAnn.parent() : 
									 entity != null? entity.parent() : 
									 ROOT_ANCESTORS; 
				if (cparents.length > 0) {
					Set<String> parents = Sets.newTreeSet();
					for (Class<?> clazz : cparents) {
						parents.add(repository.get(clazz).getKind());
					}
					metadata.setParents(parents);
				}
				
			}
			
			instance = repository;
			return repository;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static PersistenceMetadataRepository getPersistenceMetadataRepository() {
		return instance;
	}

	public void setResolver(ResourcePatternResolver resolver) {
		this.resolver = resolver;
	}

	public void setLocations(String[] locations) {
		this.locations = locations;
	}

	public void setClassMetadataFactory(ClassMetadataFactory classMetadataFactory) {
		this.classMetadataFactory = classMetadataFactory;
	}

	
}
