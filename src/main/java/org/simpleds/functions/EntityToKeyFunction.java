package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into its Key 
 * @param <T> the entity class type
 * @author icoloma
 *
 */
public class EntityToKeyFunction<T> extends AbstractPropertyFunction<T, Key> {
	
	/**
	 * Iterate over a consistent collection where all items share the same class.
	 * @param clazz the entity class type
	 */
	public EntityToKeyFunction(Class<T> clazz) {
		super(null);
	}
	
	/**
	 * Iterate a mixed collection where items can be of different types
	 */
	public EntityToKeyFunction() {
		super(null);
	}
	
}