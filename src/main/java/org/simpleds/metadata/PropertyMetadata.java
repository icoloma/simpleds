package org.simpleds.metadata;

import java.lang.annotation.Annotation;

import org.simpleds.converter.Converter;

/**
 * A handler for a simple or nested persistent property
 * @author icoloma
 */
public interface PropertyMetadata {

	/**
	 * @param container the container of this property
	 * @return the value of the property in the specified container
	 */
	public Object getValue(Object container);
	
	/**
	 * @param container the container of this property
	 * @param value the value to set into this property
	 */
	public void setValue(Object container, Object value);

	/**
	 * @return the persistent name of this property
	 */
	public String getName();

	/**
	 * @return the Converter to use for this property
	 */
	public Converter getConverter();

	/**
	 * @return if the last node in the path contains the provided annotation, 
	 * return that. Otherwise, return null. 
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass);
	
	/**
	 * @return the type of the last node in the path
	 */
	public Class getPropertyType();

}
