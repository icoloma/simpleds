package org.simpleds.converter;


public class EnumToStringConverter implements Converter<Enum, String> {

	/** the enum class this converter converts to/from */
	private Class<Enum> enumClass;
	
	public EnumToStringConverter(Class<Enum> enumClass) {
		this.enumClass = enumClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enum datastoreToJava(String name) {
		return name == null? null : Enum.valueOf(enumClass, name);
	}

	@Override
	public String javaToDatastore(Enum value) {
		return value == null? null : value.toString();
	}


}
