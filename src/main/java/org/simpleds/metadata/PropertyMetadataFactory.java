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
			
			// calculate the property type
			Class<?> propertyType = getter == null? field.getType() : getter.getReturnType();		
			metadata.setPropertyType(propertyType);
			
			// no setter specified, we will set the field directly 
			if (setter == null) {
				field.setAccessible(true);
			}
			return metadata;
		} catch (RuntimeException e) {
			throw new RuntimeException("Cannot process property '" + name + "': " + e.getMessage(), e);
		}
	}

	private static void initPropertyType(SinglePropertyMetadata metadata) {
	}
	
	
/*
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE
 */
	
}
