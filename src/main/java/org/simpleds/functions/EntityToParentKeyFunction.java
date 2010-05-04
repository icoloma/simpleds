package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into its parent Key 
 * @param <T> the entity class type
 * @author icoloma
 */
public class EntityToParentKeyFunction<T> extends AbstractPropertyFunction<T, Key> {
	
	/**
	 * @param clazz the entity class type
	 */
	public EntityToParentKeyFunction(Class<T> clazz) {
		super(clazz, null);
	}
	
	@Override
	public Key apply(T instance) {
		return (propertyMetadata.getValue(instance)).getParent();
	}
	
}