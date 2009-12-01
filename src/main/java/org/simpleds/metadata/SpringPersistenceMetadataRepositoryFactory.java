package org.simpleds.metadata;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Wrapper that makes injection of {@link PersistenceMetadataRepository} attributes easier using spring.
 * @author icoloma
 *
 */
public class SpringPersistenceMetadataRepositoryFactory implements FactoryBean {

	private PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();

	@Override
	public Object getObject() throws Exception {
		return factory.initialize();
	}

	@Override
	public Class getObjectType() {
		return PersistenceMetadataRepository.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	@Autowired
	public void setResolver(ResourcePatternResolver resolver) {
		factory.setResolver(resolver);
	}

	public void setLocations(String[] locations) {
		factory.setLocations(locations);
	}

	@Autowired(required=false)
	public void setClassMetadataFactory(ClassMetadataFactory classMetadataFactory) {
		factory.setClassMetadataFactory(classMetadataFactory);
	}
	
}
