package org.simpleds.functions;

import org.simpleds.EntityManagerFactory;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;

import com.google.common.base.Function;

/**
 * Return a property value 
 * 
 * @author icoloma
 *
 * @param <T> the type  of the Java Object
 * @param <P> the type of the property to return
 */
public abstract class AbstractPropertyFunction<T, P> implements Function<T, P> {
	
	protected PropertyMetadata<P, ?> propertyMetadata;
	
	@SuppressWarnings("unchecked")
	public AbstractPropertyFunction(Class<T> clazz, String propertyName) {
		ClassMetadata classMetadata = EntityManagerFactory.getEntityManager().getClassMetadata(clazz);
		this.propertyMetadata = (PropertyMetadata<P, ?>) (propertyName == null? classMetadata.getKeyProperty() : classMetadata.getProperty(propertyName));
	}
	
}