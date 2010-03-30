package org.simpleds.functions;

import org.simpleds.EntityManagerFactory;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Converts a list of persistent instances into their parent Keys 
 * @author icoloma
 *
 */
public class EntityParentFunction<T> implements Function<T, Key> {
	
	private PropertyMetadata keyMetadata;
	
	public EntityParentFunction(Class<T> clazz) {
		ClassMetadata classMetadata = EntityManagerFactory.getEntityManager().getClassMetadata(clazz);
		this.keyMetadata = classMetadata.getKeyProperty();
	}
	
	@Override
	public Key apply(T instance) {
		return ((Key)keyMetadata.getValue(instance)).getParent();
	}
	
}