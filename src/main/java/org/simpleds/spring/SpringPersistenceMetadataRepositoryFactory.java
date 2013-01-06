package org.simpleds.spring;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;
import org.simpleds.converter.ConverterFactory;
import org.simpleds.metadata.ClassMetadataFactory;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.util.ClassUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * Wrapper that makes injection of {@link PersistenceMetadataRepository} attributes easier using spring.
 * @author icoloma
 *
 */
@Singleton
public class SpringPersistenceMetadataRepositoryFactory implements FactoryBean<PersistenceMetadataRepository> {

	@Inject
	private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	@Autowired(required=false)
	private ClassMetadataFactory classMetadataFactory;
	
	/** the list of ant-style package locations, such as classpath*:com/acme/model/** */
	private String[] locations;
	
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	@PostConstruct
	public void initialize() {
		try {
			if (locations == null) {
				throw new IllegalArgumentException("locations has not been specified");
			}
			if (classMetadataFactory == null) {
				ConverterFactory converterFactory = new ConverterFactory();
                converterFactory.setObjectMapper(new ObjectMapper());
				classMetadataFactory = new ClassMetadataFactory();
				classMetadataFactory.setConverterFactory(converterFactory);
			}
			persistenceMetadataRepository = new PersistenceMetadataRepository();
			persistenceMetadataRepository.setClassMetadataFactory(classMetadataFactory);
			MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();
			for (String location : locations) {
				Resource[] resources = resolver.getResources(location);
				for (Resource resource : resources) {
					if (resource.isReadable()) {
						if (!resource.getFilename().endsWith(".class")) {
							throw new IllegalArgumentException("The resource " + resource.getDescription() + " is not a valid class file");
						}
						MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
						AnnotationMetadata am = metadataReader.getAnnotationMetadata();
						if (am.hasAnnotation(org.simpleds.annotations.Entity.class.getName())) {
							Class<?> clazz = ClassUtils.getDefaultClassLoader().loadClass(am.getClassName());
							persistenceMetadataRepository.add(clazz);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public PersistenceMetadataRepository getObject() {
		return persistenceMetadataRepository;
	}

	@Override
	public Class<PersistenceMetadataRepository> getObjectType() {
		return PersistenceMetadataRepository.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	public void setLocations(String[] locations) {
		this.locations = locations;
	}

	public void setClassMetadataFactory(ClassMetadataFactory classMetadataFactory) {
		this.classMetadataFactory = classMetadataFactory;
	}
	
	public void setResolver(ResourcePatternResolver resolver) {
		this.resolver = resolver;
	}


}
