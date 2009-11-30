package org.simpleds.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.converter.IntegerConverter;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ClassMetadataTest extends AbstractDatastoreTest {

	private ClassMetadata metadata;
	
	private PersistenceMetadataRepository repository;
	
	@Before
	public void setup() {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[] { "classpath*:org/simpleds/testdb/**" });
		repository = factory.createRepository();
		metadata = repository.get(Dummy1.class);
	}
	
	@Test
	public void testRequiredProperties() throws Exception {
		Set<String> rp = metadata.getRequiredProperties();
		assertTrue(rp.contains("name"));
		assertTrue(rp.contains("date"));
	}
	
	@Test
	public void testEmbeddedProperties() throws Exception {
		metadata.getProperty("int2");
		PropertyMetadata property = metadata.getProperty("int1");
		assertTrue(property.getConverter() instanceof IntegerConverter);
		Dummy1 dummy = new Dummy1();
		assertNull(property.getValue(dummy));
		property.setValue(dummy, Integer.valueOf(1));
		assertEquals(1, property.getValue(dummy));
	}
	
	@Test
	public void testDatastoreToJava() throws Exception {
		Date d = new Date();
		Key key = KeyFactory.createKey(Dummy1.class.getSimpleName(), 1);
		Entity entity = new Entity(key);
		entity.setProperty("name", "foo");
		entity.setProperty("date", d);
		entity.setProperty("int1", Long.valueOf(1));
		entity.setProperty("int2", Long.valueOf(2));
		entity.setProperty("xxx", "foobar"); // ignored property that is not mapped
		Dummy1 dummy = metadata.datastoreToJava(entity);
		assertEquals(key, dummy.getKey());
		assertEquals("foo", dummy.getName());
		assertSame(d, dummy.getDate());
		assertEquals(1, dummy.getEmbedded().getInt1());
		assertEquals(Integer.valueOf(2), dummy.getEmbedded().getEmbedded2().int2);
	}
	
	@Test
	public void testJavaToDatastore() throws Exception {
		Dummy1 dummy = new Dummy1();
		Key key = new KeyFactory.Builder(Dummy1.class.getSimpleName(), 1L).getKey();
		dummy.setKey(key);
		dummy.setName("foo");
		Entity entity = metadata.javaToDatastore(null, dummy);
		assertEquals(key, entity.getKey());
		assertNull(entity.getProperty("id"));
		assertEquals("foo", entity.getProperty("name"));
	}

	
}
