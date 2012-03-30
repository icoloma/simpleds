package org.simpleds.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;
import org.simpleds.test.AbstractDatastoreTest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ConverterTest extends AbstractDatastoreTest {
	
	private ConverterFactory converterFactory = new ConverterFactory();
	
	@Test
	public void testNull() throws Exception {
		Field convertersField = ConverterFactory.class.getDeclaredField("converters");
		convertersField.setAccessible(true);
		Map<Class, Converter>  converters = (Map<Class, Converter>) convertersField.get(converterFactory);
		for (Converter converter : converters.values()) {
			assertNull(converter.javaToDatastore(null));
			Object nullValue = converter instanceof AbstractConverter? ((AbstractConverter) converter).getNullValue() : null;
			assertEquals(nullValue, converter.datastoreToJava(null));
		}
	}
	
	@Test
	public void testConverters() throws Exception {
		assertConvert(Integer.valueOf(5), Long.valueOf(5));
		assertConvert("foo", "foo");
		Key key = new KeyFactory.Builder("foo", 1).getKey();
		assertConvert(key, key);
	}

	private void assertConvert(Object javaValue, Object dsValue) {
		Converter converter = converterFactory.getConverter(javaValue.getClass());
		assertEquals(javaValue, converter.datastoreToJava(dsValue));
		assertEquals(dsValue, converter.javaToDatastore(javaValue));
	}
	
}
