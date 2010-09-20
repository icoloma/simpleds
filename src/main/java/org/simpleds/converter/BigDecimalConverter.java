package org.simpleds.converter;

import java.math.BigDecimal;

public class BigDecimalConverter extends AbstractConverter<BigDecimal, String> {

	@Override
	public BigDecimal datastoreToJava(String persistentValue) {
		return persistentValue == null? null : new BigDecimal(persistentValue);
	}

	@Override
	public String javaToDatastore(BigDecimal javaValue) {
		return javaValue == null? null : javaValue.toString();
	}

	@Override
	public Class<BigDecimal> getJavaType() {
		return BigDecimal.class;
	}

	@Override
	public Class<String> getDatastoreType() {
		return String.class;
	}

}
