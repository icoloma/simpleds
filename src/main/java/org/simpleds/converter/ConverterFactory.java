package org.simpleds.converter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.simpleds.annotations.AsJSON;
import org.simpleds.metadata.SinglePropertyMetadata;
import org.simpleds.util.GenericCollectionTypeResolver;
import org.simpleds.util.MethodParameter;

import com.google.appengine.api.blobstore.BlobKey;
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

@Singleton
@SuppressWarnings("rawtypes")
public class ConverterFactory {
	
	@Inject
	private ObjectMapper objectMapper;

	private Map<Class, Converter> converters = Maps.newHashMap();
	
	public ConverterFactory() {
		addConverter(Boolean.TYPE, new NullConverter<Boolean>(Boolean.class));
		addConverter(Boolean.class, new NullConverter<Boolean>(Boolean.class));
		addConverter(Short.TYPE, new ShortConverter().setNullValue((short)0));
		addConverter(Short.class, new ShortConverter());
		addConverter(Integer.TYPE, new IntegerConverter().setNullValue(0));
		addConverter(Integer.class, new IntegerConverter());
		addConverter(Long.TYPE, new NullConverter<Long>(Long.class).setNullValue(0l));
		addConverter(Long.class, new NullConverter<Long>(Long.class));
		addConverter(Float.TYPE, new NullConverter<Float>(Float.class).setNullValue(0f));
		addConverter(Float.class, new NullConverter<Float>(Float.class));
		addConverter(Double.TYPE, new NullConverter<Double>(Double.class));
		addConverter(Double.class, new NullConverter<Double>(Double.class).setNullValue(0d));
		addConverter(Date.class, new NullConverter<Date>(Date.class));
		addConverter(String.class, new NullConverter<String>(String.class));
		addConverter(BigDecimal.class, new BigDecimalConverter());
		
		// native Google classes
		addConverter(Key.class, new NullConverter<Key>(Key.class));
		addConverter(BlobKey.class, new NullConverter<BlobKey>(BlobKey.class));
		addConverter(GeoPt.class, new NullConverter<GeoPt>(GeoPt.class));
		addConverter(Link.class, new NullConverter<Link>(Link.class));
		addConverter(PostalAddress.class, new NullConverter<PostalAddress>(PostalAddress.class));
		addConverter(Email.class, new NullConverter<Email>(Email.class));
		addConverter(IMHandle.class, new NullConverter<IMHandle>(IMHandle.class));
		addConverter(PhoneNumber.class, new NullConverter<PhoneNumber>(PhoneNumber.class));
		addConverter(Rating.class, new NullConverter<Rating>(Rating.class));
		addConverter(Blob.class, new NullConverter<Blob>(Blob.class));
		addConverter(Text.class, new NullConverter<Text>(Text.class));
		addConverter(User.class, new NullConverter<User>(User.class));
	}
	
	/**
	 * Adds a new Converter to the current config
	 */
	public void addConverter(Class clazz, Converter converter) {
		converters.put(clazz, converter);
	}
	
	/**
	 * @return the converter for a PropertyMetadata instance
	 */
	public <J, D> Converter<J, D> getConverter(SinglePropertyMetadata<J, D> metadata) {
		Class<J> propertyType = metadata.getPropertyType();
		if (metadata.getAnnotation(AsJSON.class) != null) {
			TypeFactory typeFactory = objectMapper.getTypeFactory();
			JavaType type;
			if (Map.class.isAssignableFrom(propertyType)) {
				Class[] mapKeyValue = guessMapGenericType(metadata);
				type = typeFactory.constructMapType(Map.class, mapKeyValue[0], mapKeyValue[1]);
			} else if (Set.class.isAssignableFrom(propertyType)) {
				type = typeFactory.constructCollectionType(Set.class, guessCollectionGenericType(metadata));
			} else if (Collection.class.isAssignableFrom(propertyType)) {
					type = typeFactory.constructCollectionType(List.class, guessCollectionGenericType(metadata));
			} else {
				type = typeFactory.uncheckedSimpleType(propertyType);
			}
			return new JsonConverter(type, objectMapper);
		}
		if (Collection.class.isAssignableFrom(propertyType)) {
			return getCollectionConverter((Class<? extends Iterable>) propertyType, guessCollectionGenericType(metadata));
		} else {
			return getConverter(propertyType);
		}

	}
	
	@SuppressWarnings("unchecked")
	public <J, D> Converter<J, D> getConverter(Class<J> clazz) {
		if (Enum.class.isAssignableFrom(clazz)) {
			return new EnumToStringConverter(clazz);
		}
		Converter converter = converters.get(clazz);
		if (converter == null) {
			throw new IllegalArgumentException("Cannot find configured converter for " + clazz.getName());
		}
		return converter;
	}
	
	public CollectionConverter getCollectionConverter(Class<? extends Iterable> collectionType, Class<?> itemType) {
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
	
	private Class guessCollectionGenericType(SinglePropertyMetadata metadata) {
		if (metadata.getGetter() != null) {
			return GenericCollectionTypeResolver.getCollectionReturnType(metadata.getGetter());
		} else if (metadata.getSetter() != null) {
			return GenericCollectionTypeResolver.getCollectionParameterType(new MethodParameter(metadata.getSetter(), 0));
		} else if (metadata.getField() != null) {
			return GenericCollectionTypeResolver.getCollectionFieldType(metadata.getField());
		}
		return null;
	}
	
	private Class[] guessMapGenericType(SinglePropertyMetadata metadata) {
		Method getter = metadata.getGetter();
		Method setter = metadata.getSetter();
		Field field = metadata.getField();
		if (getter != null) {
			return new Class[] { 
				GenericCollectionTypeResolver.getMapKeyReturnType(getter), 
				GenericCollectionTypeResolver.getMapValueReturnType(getter) 
			};
		} else if (setter != null) {
			MethodParameter methodParam = new MethodParameter(setter, 0);
			return new Class[] { 
				GenericCollectionTypeResolver.getMapKeyParameterType(methodParam),
				GenericCollectionTypeResolver.getMapValueParameterType(methodParam),
			};
		} else if (field != null) {
			return new Class[] {
				GenericCollectionTypeResolver.getMapKeyFieldType(field),
				GenericCollectionTypeResolver.getMapValueFieldType(field)
			};
		}
		return null;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}
