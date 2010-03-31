package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into its parent Key 
 * @author icoloma
 *
 */
public class EntityToParentKeyFunction<T> extends AbstractPropertyFunction<T, Key> {
	
	public EntityToParentKeyFunction(Class<T> clazz) {
		super(clazz, null);
	}
	
	@Override
	public Key apply(T instance) {
		return ((Key)propertyMetadata.getValue(instance)).getParent();
	}
	
}