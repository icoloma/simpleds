package org.simpleds.converter;

import java.util.Collection;

/**
 * Collection converter
 * @author icoloma
 *
 * @param <C> the collection type
 */
public interface CollectionConverter<C extends Collection> extends Converter<C, C> {
	
	/**
	 * Create a new Collection
	 * @param size the size of the source collection
	 */
	public abstract C createCollection(int size);

	/**
	 * @return the expected type of the collection items
	 */
	public Class<?> getItemType();
	
	/**
	 * Convert one collection item from Google representation to a Java value
	 * @param value the value persistent in the google datastore
	 */
	public Object itemDatastoreToJava(Object value);
	
	/**
	 * Convert one collection item from Java representation to a Datastore value
	 * @param value the Java property value
	 */
	public Object itemJavaToDatastore(Object value);
}
