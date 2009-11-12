package org.simpleds.converter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.simpleds.metadata.SinglePropertyMetadata;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.common.collect.Maps;

public class ConverterFactory {

	private static Map<Class, Converter> converters = Maps.newHashMap();
	
	static {
		addConverter(Boolean.TYPE, new NullConverter());
		addConverter(Boolean.class, new NullConverter());
		addConverter(Short.TYPE, new ShortConverter());
		addConverter(Short.class, new ShortConverter());
		addConverter(Integer.TYPE, new IntegerConverter());
		addConverter(Integer.class, new IntegerConverter());
		addConverter(Long.TYPE, new NullConverter());
		addConverter(Long.class, new NullConverter());
		addConverter(Float.TYPE, new NullConverter());
		addConverter(Float.class, new NullConverter());
		addConverter(Double.TYPE, new NullConverter());
		addConverter(Double.class, new NullConverter());
		addConverter(Date.class, new NullConverter());
		addConverter(String.class, new NullConverter());
		addConverter(BigDecimal.class, new BigDecimalConverter());
		
		// native Google classes
		addConverter(Key.class, new NullConverter());
		addConverter(GeoPt.class, new NullConverter());
		addConverter(Link.class, new NullConverter());
		addConverter(PostalAddress.class, new NullConverter());
		addConverter(Email.class, new NullConverter());
		addConverter(IMHandle.class, new NullConverter());
		addConverter(PhoneNumber.class, new NullConverter());
		addConverter(Rating.class, new NullConverter());
		addConverter(Blob.class, new NullConverter());
		addConverter(Text.class, new NullConverter());
		addConverter(User.class, new NullConverter());
	}
	
	/**
	 * @return the converter for a PropertyMetadata instance
	 */
	public static Converter getConverter(SinglePropertyMetadata metadata) {
		// assign converter
		Class propertyType = metadata.getPropertyType();
		if (Collection.class.isAssignableFrom(propertyType)) {
			return ConverterFactory.getCollectionConverter(propertyType, guessCollectionGenericType(metadata));
		} else {
			return ConverterFactory.getConverter(propertyType);
		}

	}
	
	@SuppressWarnings("unchecked")
	public static Converter getConverter(Class clazz) {
		if (Enum.class.isAssignableFrom(clazz)) {
			return new EnumToStringConverter(clazz);
		}
		Converter converter = converters.get(clazz);
		if (converter == null) {
			throw new IllegalArgumentException("Cannot find configured converter for " + clazz.getName());
		}
		return converter;
	}
	
	public static CollectionConverter getCollectionConverter(Class collectionType, Class itemType) {
		if (itemType == null) {
			throw new IllegalArgumentException("Cannot create collection converter for unspecified node type");
		}
		AbstractCollectionConverter c;
		if (List.class.isAssignableFrom(collectionType)) {
			c = new ListConverter();
		} else if (SortedSet.class.isAssignableFrom(collectionType)) {
			c = new SortedSetConverter();
		} else if (Set.class.isAssignableFrom(collectionType)) {
			c = new SetConverter();
		} else {
			throw new IllegalArgumentException("Unrecognized collection class: " + collectionType.getName());
		}
		c.setItemConverter(getConverter(itemType));
		c.setItemType(itemType);
		return c;
	}	
	
	private static Class guessCollectionGenericType(SinglePropertyMetadata metadata) {
		if (metadata.getGetter() != null) {
			return GenericCollectionTypeResolver.getCollectionReturnType(metadata.getGetter());
		} else if (metadata.getSetter() != null) {
			return GenericCollectionTypeResolver.getCollectionParameterType(new MethodParameter(metadata.getSetter(), 0));
		} else if (metadata.getField() != null) {
			return GenericCollectionTypeResolver.getCollectionFieldType(metadata.getField());
		}
		return null;
	}
	
	/**
	 * Adds a new Converter to the current config
	 */
	public static void addConverter(Class clazz, Converter converter) {
		converters.put(clazz, converter);
	}


	
}
