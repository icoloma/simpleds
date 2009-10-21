package org.simpleds.converter;

public class NullConverter implements Converter<Object, Object> {

	@Override
	public Object datastoreToJava(Object value) {
		return value;
	}

	@Override
	public Object javaToDatastore(Object value) {
		return value;
	}



}
