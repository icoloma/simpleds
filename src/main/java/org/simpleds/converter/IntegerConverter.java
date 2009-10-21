package org.simpleds.converter;

public class IntegerConverter implements Converter<Integer, Long> {

	@Override
	public Integer datastoreToJava(Long persistentValue) {
		return persistentValue == null? null : persistentValue.intValue();
	}

	@Override
	public Long javaToDatastore(Integer javaValue) {
		return javaValue == null? null : javaValue.longValue();
	}

}
