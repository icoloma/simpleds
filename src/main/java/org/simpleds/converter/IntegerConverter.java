package org.simpleds.converter;

public class IntegerConverter extends AbstractConverter<Integer, Long> {

	@Override
	public Integer datastoreToJava(Long persistentValue) {
		return persistentValue == null? (Integer) nullValue : (Integer) persistentValue.intValue();
	}

	@Override
	public Long javaToDatastore(Integer javaValue) {
		return javaValue == null? null : javaValue.longValue();
	}
	
	@Override
	public IntegerConverter setNullValue(Integer nullValue) {
		return (IntegerConverter) super.setNullValue(nullValue);
	}

}
