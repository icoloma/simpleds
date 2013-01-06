package org.simpleds.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;
import org.simpleds.converter.IntegerConverter;
import org.simpleds.exception.ConfigException;
import org.simpleds.exception.DuplicateException;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ClassMetadataTest extends AbstractEntityManagerTest {

	private ClassMetadata metadata;
	
	@Before
	public void setupMetadata() {
		metadata = repository.get(Dummy1.class);
	}
	
	@Test
	public void testRequiredProperties() throws Exception {
		Set<String> rp = metadata.getRequiredProperties();
		assertTrue(rp.contains("name"));
		assertTrue(rp.contains("date"));
	}
	
	@Test
	public void testUnindexedProperties() throws Exception {
		assertTrue(metadata.getProperty("name").isIndexed());
		assertFalse(metadata.getProperty("bigString").isIndexed());
		assertTrue(metadata.getProperty("int1").isIndexed());
		assertFalse(metadata.getProperty("int2").isIndexed());
	}
	
	@Test
	public void testEmbeddedProperties() throws Exception {
		PropertyMetadata property = metadata.getProperty("int1");
		PropertyMetadata embeddedName = metadata.getProperty("embeddedName");
		assertTrue(property.getConverter() instanceof IntegerConverter);
		Dummy1 dummy = new Dummy1();
		assertEquals(0, property.getValue(dummy));
		assertEquals(null, embeddedName.getValue(dummy));
		assertEquals(null, metadata.getProperty("int2").getValue(dummy));
		
		property.setValue(dummy, Integer.valueOf(1));
		assertEquals(1, property.getValue(dummy));
	}
	
	@Test
	public void testDatastoreToJava() throws Exception {
		Date d = new Date();
		Key key = KeyFactory.createKey(Dummy1.KIND, 1);
		Entity entity = new Entity(key);
		entity.setProperty("name", "foo");
		entity.setProperty("date", d);
		entity.setProperty("int1", Long.valueOf(1));
		entity.setProperty("int2", Long.valueOf(2));
		entity.setProperty("bigString", "foobar");
		entity.setProperty("xxx", "foobar"); // ignored property that is not mapped
		Dummy1 dummy = metadata.datastoreToJava(entity);
		assertEquals(key, dummy.getKey());
		assertEquals("foo", dummy.getName());
		assertSame(d, dummy.getOverridenNameDate());
		assertEquals(1, dummy.getEmbedded().getI1());
		assertEquals(Integer.valueOf(2), dummy.getEmbedded().getEmbedded2().i2);
		assertEquals("foobar", dummy.getBigString());
	}
	
	@Test
	public void testJavaToDatastore() throws Exception {
		Dummy1 dummy = new Dummy1();
		Key key = new KeyFactory.Builder(Dummy1.KIND, 1L).getKey();
		dummy.setKey(key);
		dummy.setName("foo");
		Entity entity = metadata.javaToDatastore(null, dummy);
		assertEquals(key, entity.getKey());
		assertNull(entity.getProperty("id"));
		assertEquals("foo", entity.getProperty("name"));
	}
	
	@Test(expected=DuplicateException.class)
	public void testRepeatedKind() throws Exception {
		repository.add(RepeatedKind.class);
	}
	
	@Test(expected=ConfigException.class)
	public void testRepeatedProperty() throws Exception {
		repository.add(RepeatedProperty.class);
	}

	@org.simpleds.annotations.Entity("d2")
	static class RepeatedKind {
		@Id
		private Key key;
	}
	
	static class RepeatedProperty {
		@Id
		private Key key;
		
		private String foo;
		
		@Property("foo")
		private String bar;
	}
	
}
