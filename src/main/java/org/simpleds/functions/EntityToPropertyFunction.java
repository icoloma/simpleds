package org.simpleds.functions;


/**
 * Converts a persistent instance into a property value
 * @author icoloma
 *
 */
public class EntityToPropertyFunction<T, P> extends AbstractPropertyFunction<T, P> {
	
	public EntityToPropertyFunction(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}
	
	@Override
	public P apply(T instance) {
		return (P) propertyMetadata.getValue(instance);
	}
	
}