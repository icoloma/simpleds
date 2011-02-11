package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into its parent Key 
 * @param <T> the entity class type
 * @author icoloma
 */
public class EntityToParentKeyFunction<T> extends EntityToKeyFunction<T> {
	
	/**
	 * Iterate over a consistent collection where all items share the same class.
	 * @param clazz the entity class type
	 */
	public EntityToParentKeyFunction(Class<T> clazz) {
		super(null);
		consistent();
	}
	
	/**
	 * Iterate over a mixed collection where items can be of different types
	 */
	public EntityToParentKeyFunction() {
		super(null);
	}
	
	@Override
	public Key apply(T instance) {
		Key k = super.apply(instance);
		return k == null? null : k.getParent();
	}
	
}