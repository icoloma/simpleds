package org.simpleds.converter;

public class ShortConverter implements Converter<Short, Long> {

	@Override
	public Short datastoreToJava(Long persistentValue) {
		return persistentValue == null? null : persistentValue.shortValue();
	}

	@Override
	public Long javaToDatastore(Short javaValue) {
		return javaValue == null? null : javaValue.longValue();
	}

}
