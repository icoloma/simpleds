package org.simpleds.converter;

public class NullConverter<J, D> extends AbstractConverter<J, D> {

	public J datastoreToJava(D value) {
		return value == null? nullValue : (J) value;
	}
	
	@Override
	public D javaToDatastore(J value) {
		return (D) value;
	}

}
