package org.simpleds.converter;

public abstract class AbstractConverter<J, D> implements Converter<J,D> {

	protected J nullValue;

	public J getNullValue() {
		return nullValue;
	}

	public AbstractConverter<J, D> setNullValue(J nullValue) {
		this.nullValue = nullValue;
		return this;
	}
	
}
