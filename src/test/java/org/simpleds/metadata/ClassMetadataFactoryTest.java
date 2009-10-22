package org.simpleds.metadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.simpleds.annotations.MultivaluedIndex;
import org.simpleds.annotations.Relations;
import org.simpleds.converter.AbstractCollectionConverter;
import org.simpleds.converter.NullConverter;

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
		MultivaluedIndexMetadata index = metadata.getRelationIndex("dummies1");
		assertNotNull(index);
		assertTrue(((AbstractCollectionConverter)index.getConverter()).getItemConverter() instanceof NullConverter);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testKeyNotAvailableAsProperty() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		metadata.getProperty("id");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testTransient() throws Exception {
		ClassMetadata metadata = factory.createMetadata(MyClass.class);
		metadata.getProperty("xxx");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDoublePrimaryKey() throws Exception {
		ClassMetadata metadata = factory.createMetadata(Error1.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNoPrimaryKey() throws Exception {
		ClassMetadata metadata = factory.createMetadata(Error2.class);
	}
	
	public static class Parent {
		private Integer foo;
	}
	
	@Relations(@MultivaluedIndex(value="dummies1"))
	public static class MyClass extends Parent {
		
		@Id
		private Key key;
		
		@Transient
		private String xxx;

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
