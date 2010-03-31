package org.simpleds.functions;

import org.simpleds.EntityManagerFactory;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;

import com.google.common.base.Function;

/**
 * Converts a list of persistent instances into their parent Keys 
 * @author icoloma
 *
 */
public abstract class AbstractPropertyFunction<T, P> implements Function<T, P> {
	
	protected PropertyMetadata propertyMetadata;
	
	public AbstractPropertyFunction(Class<T> clazz, String propertyName) {
		ClassMetadata classMetadata = EntityManagerFactory.getEntityManager().getClassMetadata(clazz);
		this.propertyMetadata = propertyName == null? classMetadata.getKeyProperty() : classMetadata.getProperty(propertyName);
	}
	
}