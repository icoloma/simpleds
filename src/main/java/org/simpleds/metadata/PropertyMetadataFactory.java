package org.simpleds.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyMetadataFactory {

	private static Log log = LogFactory.getLog(PropertyMetadataFactory.class);
	
	public static SinglePropertyMetadata create(String name, Field field, Method getter, Method setter) {
		try {
			log.debug("Processing property " + name);
			if (field == null && getter == null) {
				throw new IllegalArgumentException("Either supply field or getter");
			}
			SinglePropertyMetadata metadata = new SinglePropertyMetadata();
			metadata.setName(name);
			metadata.setGetter(getter);
			metadata.setSetter(setter);
			metadata.setField(field);
			
			// no setter specified, we will set the field directly 
			if (setter == null) {
				field.setAccessible(true);
			}
			return metadata;
		} catch (RuntimeException e) {
			throw new RuntimeException("Cannot process property '" + name + "': " + e.getMessage(), e);
		}
	}
	

	
}
