package org.simpleds.converter;

import java.math.BigDecimal;

public class BigDecimalConverter implements Converter<BigDecimal, String> {

	@Override
	public BigDecimal datastoreToJava(String persistentValue) {
		return persistentValue == null? null : new BigDecimal(persistentValue);
	}

	@Override
	public String javaToDatastore(BigDecimal javaValue) {
		return javaValue == null? null : javaValue.toString();
	}

}
