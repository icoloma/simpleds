package org.simpleds.converter;

public class NullConverter<J> extends AbstractConverter<J, J> {

	@Override
	public J datastoreToJava(J value) {
		return value == null? nullValue : (J) value;
	}

	@Override
	public J javaToDatastore(J value) {
		return value;
	}

}
