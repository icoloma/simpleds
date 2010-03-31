package org.simpleds.functions;

import org.simpleds.EntityManagerFactory;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into its parent Key 
 * @author icoloma
 *
 */
public class EntityToParentEntityFunction<T> extends AbstractPropertyFunction<T, Key> {
	
	public EntityToParentEntityFunction(Class<T> clazz) {
		super(clazz, null);
	}
	
	@Override
	public Key apply(T instance) {
		Key parentKey = ((Key)propertyMetadata.getValue(instance)).getParent();
		return EntityManagerFactory.getEntityManager().get(parentKey);
	}
	
}