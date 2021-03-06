package org.simpleds.converter;

public class NullConverter<J> extends AbstractConverter<J, J> {

	private Class<J> javaClass;
	
	public NullConverter(Class<J> javaClass) {
		this.javaClass = javaClass;
	}

	@Override
	public J datastoreToJava(J value) {
		return value == null? nullValue : (J) value;
	}

	@Override
	public J javaToDatastore(J value) {
		return value;
	}

	@Override
	public Class<J> getJavaType() {
		return javaClass;
	}

	@Override
	public Class<J> getDatastoreType() {
		return javaClass;
	}

}
