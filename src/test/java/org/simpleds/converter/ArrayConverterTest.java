package org.simpleds.converter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

public class ArrayConverterTest {

	private Date[] singleDate;
	private String[][] doubleString;
	
	private ArrayConverter converter;
	
	@Test
	public void testSingleDate() throws Exception {
		Class clazz = getClass().getDeclaredField("singleDate").getType();
		converter = (ArrayConverter) ConverterFactory.getConverter(clazz);
		
		assertConvertNull();
		assertConvert(new Date[0], new Date[0]);
		Date d = new Date();
		Date d2 = new Date();
		Date[] a1 = new Date[] { d, d2, null };
		Date[] a2 = new Date[] { d, d2, null };
		assertConvert(a1, a2);
	}

	@Test
	public void testDoubleString() throws Exception {
		Class clazz = getClass().getDeclaredField("doubleString").getType();
		converter = (ArrayConverter) ConverterFactory.getConverter(clazz);
		assertTrue(converter.getJavaType().isArray());
		assertTrue(converter.getDatastoreType().isArray());
		
		assertConvertNull();
		assertArrayEquals(new String[0][0], new String[0][0]);
		String[][] s1 = new String[][] { { "foo", null }, { "bar", "baz" } };
		String[][] s2 = new String[][] { { "foo", null }, { "bar", "baz" } };
		assertConvert(s1, s2);
	}
	
	private void assertConvert(Object[] javaValue, Object[] dsValue) {
		assertArrayEquals(dsValue, (Object[])converter.javaToDatastore(javaValue));
		assertArrayEquals(javaValue, (Object[])converter.datastoreToJava(dsValue));
	}
	
	private void assertConvertNull() {
		assertNull(converter.javaToDatastore(null));
		assertNull(converter.datastoreToJava(null));
	}
	
}
