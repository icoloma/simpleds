package org.simpleds.converter;

public class IntegerConverter implements Converter<Integer, Long> {

	private Integer nullValue;
	
	@Override
	public Integer datastoreToJava(Long persistentValue) {
		return persistentValue == null? nullValue : persistentValue.intValue();
	}

	@Override
	public Long javaToDatastore(Integer javaValue) {
		return javaValue == null? null : javaValue.longValue();
	}
	
	public IntegerConverter withNullValue(Integer nullValue) {
		this.nullValue = nullValue;
		return this;
	}

}
