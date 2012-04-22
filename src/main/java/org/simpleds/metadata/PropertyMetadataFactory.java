package org.simpleds.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.simpleds.annotations.Property;
import org.simpleds.converter.Converter;
import org.simpleds.converter.NullConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyMetadataFactory {

	private static Logger log = LoggerFactory.getLogger(PropertyMetadataFactory.class);
	
	/** maximum number of characters recommended for a property name */
	private static int MAX_PROPERTY_CHARS = 3;
	
	@SuppressWarnings("unchecked")
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
				if (propertyAnn.value().length() > 0) {
					name = propertyAnn.value();
				}
				if (propertyAnn.converter() != NullConverter.class) {
					metadata.setConverter((Converter<J, D>) propertyAnn.converter().newInstance());
				} 				
			}
			
			if (name.length() > MAX_PROPERTY_CHARS) {
				log.warn((getter != null? getter.getDeclaringClass().getSimpleName() : field.getDeclaringClass().getSimpleName() ) + 
						"." + name + " is a long name for a property. Consider using @Property to make it shorter, which will save space in the Datastore. Use " + 
						PropertyMetadataFactory.class.getSimpleName() + ".setMaxPropertyChars() to disable this warning");
			}
			metadata.setName(name);
			
			// calculate the property type
			Class<J> propertyType = (Class<J>) (getter == null? field.getType() : getter.getReturnType());
			metadata.setPropertyType(propertyType);
			
			// no setter specified, we will set the field directly 
			if (setter == null) {
				field.setAccessible(true);
			}
			return metadata;
		} catch (RuntimeException e) {
			throw new RuntimeException("Cannot process property '" + name + "': " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set maximum number of chars for a property name. Set to Integer.MAX_VALUE to disable the warning associated to long attribute names
	 */
	public static void setMaxPropertyChars(int maxChars) {
		MAX_PROPERTY_CHARS = maxChars;
	}

}
