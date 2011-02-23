package org.simpleds.functions;

import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;

import com.google.common.base.Function;

/**
 * Abstract superclass to all property-related functions
 * @param <T> the containing Java class
 * @param <P> the property class
 */
public abstract class AbstractPropertyFunction<T, P> implements Function<T, P> {
	
	/** if true, all the elements are expected to be of the same type */
	private boolean consistent;
	
	/** the property name to use */
	private String propertyName;
	
	/** if consistent == true, this is the cached property accesor to use */
	private PropertyMetadata<P, ?> propertyMetadata;
	
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public AbstractPropertyFunction(String propertyName) {
		this.propertyName = propertyName;
		entityManager = EntityManagerFactory.getEntityManager();
	}

	
	@Override
	public P apply(T instance) {
		if (instance == null) {
			return null;
		}
		PropertyMetadata<P, ?> property = getProperty(instance);
		return property.getValue(instance);
	}
	
	protected final PropertyMetadata getProperty(T instance) {
		if (consistent) {
			if (propertyMetadata == null) {
				ClassMetadata classMetadata = entityManager.getClassMetadata(instance.getClass());
				this.propertyMetadata = (PropertyMetadata<P, ?>) (propertyName == null? classMetadata.getKeyProperty() : classMetadata.getProperty(propertyName));
			}
			return propertyMetadata;
		}
		ClassMetadata classMetadata = entityManager.getClassMetadata(instance.getClass());
		return propertyName == null? classMetadata.getKeyProperty() : classMetadata.getProperty(propertyName);
	}
	
	/**
	 * Indicates that this function will be iterated over a consistent collection,
	 * which means that all collection items will share the same class.
	 * This yields better iteration performance, since it saves from retrieving
	 * the same class metadata over and over again.
	 * @return this instance
	 */
	public AbstractPropertyFunction<T, P> consistent() {
		this.consistent = true;
		return this;
	}
	
}