package org.simpleds.metadata;

import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Entity;

/**
 * Manages the {@link Version} values
 * @author icoloma
 *
 */
public interface VersionManager {

	/**
	 * @return the property annotated as {@link Version}
	 */
	public PropertyMetadata getPropertyMetadata();

	/**
	 * Validate the current version value
	 * @param currentEntity the fresh entity retrieved from the database
	 * @param javaObject the java object to get stored to disk
	 * @return the next java value to be set into javaObject after getting written to the datastore.
	 */
	public Object validateVersion(Entity currentEntity, Object javaObject);
	
	/**
	 * Return the first assigned value for a created entity
	 */
	public Object getStartValue();
	
}
