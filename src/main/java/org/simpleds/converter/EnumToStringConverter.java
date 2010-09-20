package org.simpleds.converter;


public class EnumToStringConverter<J extends Enum> extends AbstractConverter<J, String> {

	/** the enum class this converter converts to/from */
	private Class<J> enumClass;
	
	public EnumToStringConverter(Class<J> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public J datastoreToJava(String name) {
		return name == null? null : (J) Enum.valueOf(enumClass, name);
	}

	@Override
	public String javaToDatastore(J value) {
		return value == null? null : value.toString();
	}

	@Override
	public Class<J> getJavaType() {
		return enumClass;
	}

	@Override
	public Class<String> getDatastoreType() {
		return String.class;
	}


}
