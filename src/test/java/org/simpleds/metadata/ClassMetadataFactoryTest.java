package org.simpleds.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.simpleds.annotations.MultivaluedIndex;
import org.simpleds.annotations.MultivaluedIndexes;
import org.simpleds.converter.AbstractCollectionConverter;
import org.simpleds.converter.NullConverter;
import org.simpleds.exception.ConfigException;

import com.google.appengine.api.datastore.Key;

@SuppressWarnings("unused")
public class ClassMetadataFactoryTest {
	
	private static Log log = LogFactory.getLog(ClassMetadataFactoryTest.class);
	
	private ClassMetadataFactory factory = new ClassMetadataFactory();
	
	@Test
	public void testCreateMetadata() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		assertNotNull(metadata.getProperty("foo"));
		assertNotNull(metadata.getProperty("bar"));
		MultivaluedIndexMetadata index = metadata.getMultivaluedIndex("dummies1");
		assertNotNull(index);
		assertTrue(((AbstractCollectionConverter)index.getConverter()).getItemConverter() instanceof NullConverter);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testKeyNotAvailableAsProperty() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		metadata.getProperty("id");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testJpaTransient() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		metadata.getProperty("xxx");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpledsTransient() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		metadata.getProperty("yyy");
	}
	
	/*
	@Test
	public void testPrimitive() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		PropertyMetadata property = metadata.getProperty("intProperty");
		assertEquals(Integer.class, property.getPropertyType());
	}
	*/
	
	@Test(expected=ConfigException.class)
	public void testDoublePrimaryKey() throws Exception {
		ClassMetadata metadata = factory.createMetadata(Error1.class);
	}
	
	@Test(expected=ConfigException.class)
	public void testNoPrimaryKey() throws Exception {
		ClassMetadata metadata = factory.createMetadata(Error2.class);
	}
	
	@Test
	public void testPrivateAttribute() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		PropertyMetadata property = metadata.getProperty("intProperty");
		assertNotNull(property);
		MyClass instance = new MyClass();
		property.setValue(instance, 5);
		assertEquals(5, property.getValue(instance));
	}
	
	public static class Parent {
		private Integer foo;
	}
	
	@MultivaluedIndexes(@MultivaluedIndex(name="dummies1", itemClass=Key.class))
	public static class MyClass extends Parent {
		
		@Id
		private Key key;
		
		@Transient
		private String xxx;
		
		@org.simpleds.annotations.Transient
		private String yyy;
		
		// left empty on purpose
		@Column
		private int intProperty;

		public Key getBar() {
			return null;
		}

		public void setBar(Key bar) {
			// empty
		}

		public Key getKey() {
			return key;
		}

	}
	
	public static class Error1 {
		
		@Id
		private Key bar;
		
		@Id
		private Key baz;
		
	}
	
	public static class Error2 {
		
		@Id
		private Date bar;
		
	}
	
}
