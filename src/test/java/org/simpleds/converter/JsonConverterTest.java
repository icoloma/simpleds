package org.simpleds.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.junit.Before;
import org.junit.Test;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Text;

public class JsonConverterTest {

	private JsonConverter<Dummy1> converter;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Before
	public void setupFactory() {
		JavaType javaType = objectMapper.getTypeFactory().uncheckedSimpleType(Dummy1.class);
		converter = new JsonConverter<Dummy1>(javaType, objectMapper);
	}
	
	// for more complex tests, see JsonStoredTest
	
	@Test
	public void testSerializeDeserialize() {
		Dummy1 d1 = Dummy1.create();
		Text t = converter.javaToDatastore(d1);
		assertTrue(t.getValue().contains("\"name\":\"foo\""));
		Dummy1 d2 = converter.datastoreToJava(t);
		assertEquals(d1.getName(), d2.getName());
	}
	
	@Test
	public void testNull() {
		assertNull(converter.datastoreToJava(null));
		assertNull(converter.javaToDatastore(null));
	}
	
}
