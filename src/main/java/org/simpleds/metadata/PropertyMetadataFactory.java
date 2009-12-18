package org.simpleds.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Column;

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
			metadata.setGetter(getter);
			metadata.setSetter(setter);
			metadata.setField(field);
			
			// maybe override the column name using a Column annotation
			Column column = metadata.getAnnotation(Column.class);
			if (column != null && column.name().length() > 0) {
				name = column.name();
			}

			metadata.setName(name);
			
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

}
