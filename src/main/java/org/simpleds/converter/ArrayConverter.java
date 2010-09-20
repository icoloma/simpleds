package org.simpleds.converter;

import java.lang.reflect.Array;



/**
 * Array converter
 * @author icoloma
 * @param <C>
 */
@SuppressWarnings("unchecked")
public class ArrayConverter<J, D> implements Converter<J, D> {

	/** converter used for each item */
	private Converter itemConverter;
	
	/** the java array class */
	private Class<J> javaClass;
	
	/** the datastore array class */
	private Class<D> datastoreClass;
	
	public ArrayConverter(Class<J> javaClass, Converter itemConverter) {
		this.itemConverter = itemConverter;
		this.javaClass = javaClass;
		this.datastoreClass = (Class<D>) Array.newInstance(itemConverter.getDatastoreType(), 0).getClass();
	}

	@Override
	public J datastoreToJava(D dsValue) {
		if (dsValue == null) { 
			return null;
		}
		int length = Array.getLength(dsValue);
		Object[] collection = (Object[]) Array.newInstance(itemConverter.getJavaType(), length);
		for (int i = 0; i < length; i++) {
			Object javaItem = itemConverter.datastoreToJava(Array.get(dsValue, i));
			collection[i] = javaItem;
		}
		return (J) collection;
	}
	
	@Override
	public J getNullValue() {
		return null;
	}

	@Override
	public D javaToDatastore(J javaValue) {
		if (javaValue == null) { 
			return null;
		}
		if (itemConverter instanceof NullConverter) { // micro optimization
			return (D) javaValue;
		}
		int length = Array.getLength(javaValue);
		Object[] collection = (Object[]) Array.newInstance(itemConverter.getDatastoreType(), length);
		for (int i = 0; i < length; i++) {
			Object dsItem = itemConverter.javaToDatastore(Array.get(javaValue, i));
			collection[i] = dsItem;
		}
		return (D) collection;
	}

	@Override
	public Class<J> getJavaType() {
		return javaClass;
	}

	@Override
	public Class<D> getDatastoreType() {
		return datastoreClass;
	}
	
	
}
