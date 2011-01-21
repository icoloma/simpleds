package org.simpleds.metadata;

import org.simpleds.exception.OptimisticLockException;

import com.google.appengine.api.datastore.Entity;

public abstract class AbstractVersionManager<T> implements VersionManager {

	protected PropertyMetadata<T, Object> propertyMetadata;
	
	@Override
	public T validateVersion(Entity currentEntity, Object javaObject) {
		T expectedJavaValue = propertyMetadata.getValue(javaObject);
		Object dsValue = currentEntity.getProperty(propertyMetadata.getName());
		T currentJavaValue = propertyMetadata.getConverter().datastoreToJava(dsValue);
		if (expectedJavaValue == null) {
			if (currentJavaValue != null) {
				throw new OptimisticLockException(currentEntity.getKey(), expectedJavaValue, currentJavaValue);
			}
		} else if (!expectedJavaValue.equals(currentJavaValue)) {
			throw new OptimisticLockException(currentEntity.getKey(), expectedJavaValue, currentJavaValue);
		}
		return nextValue(currentJavaValue);
	}
	
	/**
	 * Create the next version value
	 * @param currentValue the last stored value. Can be null.
	 * @return the next value to be stored at the datastore
	 */
	protected abstract T nextValue(T currentValue);

	public void setPropertyMetadata(PropertyMetadata<T, Object> propertyMetadata) {
		this.propertyMetadata = propertyMetadata;
	}

	public PropertyMetadata<T, Object> getPropertyMetadata() {
		return propertyMetadata;
	}
	
}
