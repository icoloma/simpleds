package org.simpleds.functions;

import org.simpleds.EntityManagerFactory;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Abstract superclass to functions that calculate a Key from parts of it
 * @param <T> The type of the key part
 */
public abstract class AbstractPartialKeyFunction<T> implements Function<T, Key> {
	
	/** parent key, if any */
	protected Key parentKey;
	
	/** the kind to apply */
	protected String kind;
	
	public AbstractPartialKeyFunction(String kind) {
		this.kind = kind;
	}
	
	public AbstractPartialKeyFunction(Class<?> persistentClass) {
		this.kind = EntityManagerFactory.getEntityManager().getClassMetadata(persistentClass).getKind();
	}

	/**
	 * Apply the supplied parentKey to all created Keys
	 * @param parentKey the parent key to use
	 * @return this same instance, for chaining
	 */
	public AbstractPartialKeyFunction<T> withParent(Key parentKey) {
		this.parentKey = parentKey;
		return this;
	}
	
}