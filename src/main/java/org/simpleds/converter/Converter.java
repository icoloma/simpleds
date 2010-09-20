package org.simpleds.converter;

/**
 * Converter between google datastore and java types
 * @author icoloma
 */
public interface Converter<J, D> {

	/**
	 * Convert a value from Google representation to a Java value
	 * @param value the value persistent in the google datastore
	 */
	public J datastoreToJava(D value);
	
	/**
	 * Convert a value from Java representation to a Datastore value
	 * @param value the Java property value
	 */
	public D javaToDatastore(J value);
	
	/**
	 * Return the value to use for null values. This method will usually 
	 * return null, except for embedded primitive values (int, long, etc)
	 * that will return zero instead.
	 * @return The value to use in Java in case the datastore value is null.
	 */
	public J getNullValue();
	
	/**
	 * @return the java type handled by this converter
	 */
	public Class<J> getJavaType();
	
	/**
	 * @return the datastore type handled by this converter
	 */
	public Class<D> getDatastoreType();
	
}
