package org.simpleds.functions;


/**
 * Converts a persistent instance into a property value
 * 
 * @param <T> the entity class type
 * @param <P> the property type
 * @author icoloma
 *
 */
public class EntityToPropertyFunction<T, P> extends AbstractPropertyFunction<T, P> {
	
	/**
	 * @param clazz the entity class type
	 * @param propertyName the property name
	 */
	public EntityToPropertyFunction(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}
	
	@Override
	public P apply(T instance) {
		return propertyMetadata.getValue(instance);
	}
	
}