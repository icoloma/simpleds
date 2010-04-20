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
		addConverter(Boolean.TYPE, new NullConverter<Boolean>());
		addConverter(Boolean.class, new NullConverter<Boolean>());
		addConverter(Short.TYPE, new ShortConverter().setNullValue((short)0));
		addConverter(Short.class, new ShortConverter());
		addConverter(Integer.TYPE, new IntegerConverter().setNullValue(0));
		addConverter(Integer.class, new IntegerConverter());
		addConverter(Long.TYPE, new NullConverter<Long>().setNullValue(0l));
		addConverter(Long.class, new NullConverter<Long>());
		addConverter(Float.TYPE, new NullConverter<Float>().setNullValue(0f));
		addConverter(Float.class, new NullConverter<Float>());
		addConverter(Double.TYPE, new NullConverter<Double>());
		addConverter(Double.class, new NullConverter<Double>().setNullValue(0d));
		addConverter(Date.class, new NullConverter<Date>());
		addConverter(String.class, new NullConverter<Date>());
		addConverter(BigDecimal.class, new BigDecimalConverter());
		
		// native Google classes
		addConverter(Key.class, new NullConverter<Key>());
		addConverter(GeoPt.class, new NullConverter<GeoPt>());
		addConverter(Link.class, new NullConverter<Link>());
		addConverter(PostalAddress.class, new NullConverter<PostalAddress>());
		addConverter(Email.class, new NullConverter<Email>());
		addConverter(IMHandle.class, new NullConverter<IMHandle>());
		addConverter(PhoneNumber.class, new NullConverter<PhoneNumber>());
		addConverter(Rating.class, new NullConverter<Rating>());
		addConverter(Blob.class, new NullConverter<Blob>());
		addConverter(Text.class, new NullConverter<Text>());
		addConverter(User.class, new NullConverter<User>());
	}
	
	/**
	 * @return the converter for a PropertyMetadata instance
	 */
	public static <J, D> Converter<J, D> getConverter(SinglePropertyMetadata<J, D> metadata) {
		// assign converter
		Class<J> propertyType = metadata.getPropertyType();
		if (Collection.class.isAssignableFrom(propertyType)) {
			return ConverterFactory.getCollectionConverter((Class<? extends Iterable>) propertyType, guessCollectionGenericType(metadata));
		} else {
			return ConverterFactory.getConverter(propertyType);
		}

	}
	
	@SuppressWarnings("unchecked")
	public static <J, D> Converter<J, D> getConverter(Class<J> clazz) {
		if (Enum.class.isAssignableFrom(clazz)) {
			return new EnumToStringConverter(clazz);
		}
		Converter converter = converters.get(clazz);
		if (converter == null) {
			throw new IllegalArgumentException("Cannot find configured converter for " + clazz.getName());
		}
		return converter;
	}
	
	public static CollectionConverter getCollectionConverter(Class<? extends Iterable> collectionType, Class<?> itemType) {
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
