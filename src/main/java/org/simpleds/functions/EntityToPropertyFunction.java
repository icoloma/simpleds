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
	 * Iterate over a consistent collection where all items share the same class.
	 * @param clazz the entity class type
	 * @param propertyName the property name
	 */
	public EntityToPropertyFunction(Class<T> clazz, String propertyName) {
		super(propertyName);
		consistent();
	}
	
	/**
	 * Iterate over a mixed collection where items can be of different types
	 * @param propertyName the property name
	 */
	public EntityToPropertyFunction(String propertyName) {
		super(propertyName);
	}
	
}