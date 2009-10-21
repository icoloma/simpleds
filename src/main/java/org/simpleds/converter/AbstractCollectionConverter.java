package org.simpleds.converter;

import java.util.Collection;


/**
 * Collection converter
 * @author icoloma
 * @param <C>
 */
public abstract class AbstractCollectionConverter<C extends Collection> implements CollectionConverter<C> {

	/** converter used for each item */
	private Converter itemConverter;
	
	@Override
	@SuppressWarnings("unchecked")
	public C datastoreToJava(C dsValue) {
		if (dsValue == null) { // null values are translated to empty collection
			return createCollection(0);
		}
		C collection = createCollection(dsValue.size());
		for (Object o : dsValue) {
			collection.add(itemConverter.datastoreToJava(o));
		}
		return collection;
	}

	@Override
	@SuppressWarnings("unchecked")
	public C javaToDatastore(C javaValue) {
		if (javaValue == null || javaValue.isEmpty()) { //  null or empty values are not stored
			return null;
		}
		if (itemConverter instanceof NullConverter) { // micro optimization
			return javaValue;
		}
		C collection = createCollection(javaValue.size());
		for (Object o : javaValue) {
			collection.add(itemConverter.javaToDatastore(o));
		}
		return collection;
	}
	
	public Converter getItemConverter() {
		return itemConverter;
	}

	public void setItemConverter(Converter itemConverter) {
		this.itemConverter = itemConverter;
	}

}