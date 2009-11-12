package org.simpleds.converter;

import java.util.Collection;

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
}
