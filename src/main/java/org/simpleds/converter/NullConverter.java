package org.simpleds.converter;

public class NullConverter implements Converter<Object, Object> {

	private Object nullValue;

	@Override
	public Object datastoreToJava(Object value) {
		return value == null? nullValue : value;
	}

	@Override
	public Object javaToDatastore(Object value) {
		return value;
	}
	
	/**
	 * The value to use when a null value is found in the datastore.
	 * Some primitive attributes (int, long) may be found nested inside 
	 * embedded classes that are null, in which case this is the default value to use.
	 * @return
	 */
	public NullConverter withNullValue(Object nullValue) {
		this.nullValue = nullValue;
		return this;
	}

}
