package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into its Key 
 * @author icoloma
 *
 */
public class EntityToKeyFunction<T> extends AbstractPropertyFunction<T, Key> {
	
	public EntityToKeyFunction(Class<T> clazz) {
		super(clazz, null);
	}
	
	@Override
	public Key apply(T instance) {
		return (Key) propertyMetadata.getValue(instance);
	}
	
}