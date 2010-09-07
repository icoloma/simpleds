package org.simpleds.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Column;

import org.simpleds.annotations.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyMetadataFactory {

	private static Logger log = LoggerFactory.getLogger(PropertyMetadataFactory.class);
	
	public static <J, D> SinglePropertyMetadata<J, D> create(String name, Field field, Method getter, Method setter) {
		try {
			log.debug("Processing property " + name);
			if (field == null && getter == null) {
				throw new IllegalArgumentException("Either supply field or getter");
			}
			SinglePropertyMetadata<J, D> metadata = new SinglePropertyMetadata<J, D>();
			metadata.setGetter(getter);
			metadata.setSetter(setter);
			metadata.setField(field);
			
			// @Property
			Property propertyAnn = metadata.getAnnotation(Property.class);
			if (propertyAnn != null) {
				metadata.setIndexed(!propertyAnn.unindexed());
				if (propertyAnn.name().length() > 0) {
					metadata.setName(propertyAnn.name());
				}
			}
			
			// @Column 
			Column column = metadata.getAnnotation(Column.class);
			if (column != null && column.name().length() > 0) {
				name = column.name();
			}

			metadata.setName(name);
			
			// calculate the property type
			@SuppressWarnings("unchecked")
			Class<J> propertyType = (Class<J>) (getter == null? field.getType() : getter.getReturnType());
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
