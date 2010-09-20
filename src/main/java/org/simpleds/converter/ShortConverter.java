package org.simpleds.converter;

public class ShortConverter extends AbstractConverter<Short, Long> {

	@Override
	public Short datastoreToJava(Long persistentValue) {
		return persistentValue == null? (Short) nullValue : (Short) persistentValue.shortValue();
	}

	@Override
	public Long javaToDatastore(Short javaValue) {
		return javaValue == null? null : javaValue.longValue();
	}
	
	@Override
	public ShortConverter setNullValue(Short nullValue) {
		return (ShortConverter) super.setNullValue(nullValue);
	}

	@Override
	public Class<Short> getJavaType() {
		return Short.class;
	}

	@Override
	public Class<Long> getDatastoreType() {
		return Long.class;
	}
}
