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
	
}
