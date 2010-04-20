package org.simpleds.metadata;

import java.lang.annotation.Annotation;

import org.simpleds.converter.Converter;

/**
 * A handler for a simple or nested persistent property
 * @author icoloma
 */
public interface PropertyMetadata<J, D> {

	/**
	 * @param container the container of this property
	 * @return the value of the property in the specified container
	 */
	public J getValue(Object container);
	
	/**
	 * @param container the container of this property
	 * @param value the value to set into this property
	 */
	public void setValue(Object container, J value);

	/**
	 * @return the name of this property
	 */
	public String getName();
	
	/**
	 * @return the Converter to use for this property
	 */
	public Converter<J, D> getConverter();

	/**
	 * @return if the last node in the path contains the provided annotation, 
	 * return that. Otherwise, return null. 
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass);
	
	/**
	 * @return the type of the last node in the path
	 */
	public Class<?> getPropertyType();
	
	/**
	 * @return true if this property is indexed
	 */
	public boolean isIndexed();

	/**
	 * Convert a SimpleQuery parameter into something that the datastore can handle
	 */
	public Object convertQueryParam(Object value);

}
