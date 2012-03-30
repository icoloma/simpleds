package org.simpleds.converter;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import com.google.appengine.api.datastore.Text;

/**
 * Converts a property into its JSON representation, using Jackson to perform the transformation
 * @author icoloma
 *
 * @param <J>
 */
public class JsonConverter<J> implements Converter<J, Text>{
	
	/** the Jackson JavaType */
	private JavaType javaType;
	
	/** the ObjectMapper instance used for serializing/deserializing */
	private ObjectMapper objectMapper;
	
	public JsonConverter(JavaType javaType, ObjectMapper objectMapper) {
		this.javaType = javaType;
		this.objectMapper = objectMapper;
	}
	
	public JavaType getJsonJavaType() {
		return javaType;
	}
	
	@Override
	public J datastoreToJava(Text value) {
		try {
			if (value == null) {
				return null;
			}
			return (J) objectMapper.readValue(value.getValue(), getJsonJavaType());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Text javaToDatastore(J value) {
		try {
			if (value == null) {
				return null;
			}
			return new Text(objectMapper.writeValueAsString(value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public J getNullValue() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<J> getJavaType() {
		return (Class<J>) getJsonJavaType().getRawClass();
	}

	@Override
	public Class<Text> getDatastoreType() {
		return Text.class;
	}

}
