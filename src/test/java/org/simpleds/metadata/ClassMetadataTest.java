package org.simpleds.metadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.junit.Before;
import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;
import org.simpleds.converter.IntegerConverter;
import org.simpleds.exception.ConfigException;
import org.simpleds.exception.DuplicateException;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Kinds;

import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

public class ClassMetadataTest extends AbstractEntityManagerTest {

	private ClassMetadata metadata;
	
	@Before
	public void setupMetadata() {
		metadata = repository.get(Dummy1.class);
	}
	
	@Test
	public void testRequiredProperties() throws Exception {
		Set<String> rp = metadata.getRequiredProperties();
		assertTrue(rp.contains(Attrs.NAME));
		assertTrue(rp.contains(Attrs.DATE));

        assertTrue(validateEntity("foo", new Date()));
        assertFalse(validateEntity("", new Date()));
        assertFalse(validateEntity("foo", null));
	}

    private boolean validateEntity(String name, Date date) {
        Entity entity = new Entity(KeyFactory.createKey(Kinds.DUMMY1, 1));
        entity.setProperty(Attrs.NAME, name);
        entity.setProperty(Attrs.DATE, date);
        try {
            metadata.validateConstraints(entity);
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    @Test
	public void testUnindexedProperties() throws Exception {
		assertTrue(metadata.getProperty(Attrs.NAME).isIndexed());
		assertFalse(metadata.getProperty(Attrs.BIG_STRING).isIndexed());
		assertTrue(metadata.getProperty("i1").isIndexed());
		assertFalse(metadata.getProperty("i2").isIndexed());
	}
	
	@Test
	public void testEmbeddedProperties() throws Exception {
		PropertyMetadata property = metadata.getProperty("i1");
		PropertyMetadata embeddedName = metadata.getProperty(Attrs.EMBEDDED_NAME);
		assertTrue(property.getConverter() instanceof IntegerConverter);
		Dummy1 dummy = new Dummy1();
		assertEquals(0, property.getValue(dummy));
		assertEquals(null, embeddedName.getValue(dummy));
		assertEquals(null, metadata.getProperty("i2").getValue(dummy));
		
		property.setValue(dummy, Integer.valueOf(1));
		assertEquals(1, property.getValue(dummy));
	}
	
	@Test
	public void testDatastoreToJava() throws Exception {
		Date d = new Date();
		Key key = KeyFactory.createKey(Kinds.DUMMY1, 1);
		Entity entity = new Entity(key);
		entity.setProperty(Attrs.NAME, "foo");
		entity.setProperty(Attrs.DATE, d);
		entity.setProperty("i1", Long.valueOf(1));
		entity.setProperty("i2", Long.valueOf(2));
		entity.setProperty(Attrs.BIG_STRING, "foobar");
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
		Key key = new KeyFactory.Builder(Kinds.DUMMY1, 1L).getKey();
		dummy.setKey(key);
		dummy.setName("foo");
		Entity entity = metadata.javaToDatastore(null, dummy);
		assertEquals(key, entity.getKey());
		assertNull(entity.getProperty("xxx"));
		assertEquals("foo", entity.getProperty(Attrs.NAME));
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
