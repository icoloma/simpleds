package org.simpleds.metadata;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.simpleds.annotations.Cacheable;
import org.simpleds.annotations.Embedded;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.MultivaluedIndex;
import org.simpleds.annotations.MultivaluedIndexes;
import org.simpleds.annotations.Transient;
import org.simpleds.annotations.Version;
import org.simpleds.converter.Converter;
import org.simpleds.converter.ConverterFactory;
import org.simpleds.exception.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

@SuppressWarnings({ "unchecked", "rawtypes" }) 
public class ClassMetadataFactory {

	private static final Class<?>[] ROOT_ANCESTORS = new Class<?>[] {};

	private static Logger log = LoggerFactory.getLogger(ClassMetadataFactory.class);
	
	/** Maximum number of chars recommended for a kind */
	private int maxKindLength = 3;
	
	/** maximum number of characters recommended for a property name */
	private int maxPropertyLength = 3;

	@Inject
	private ConverterFactory converterFactory;
	
	public ClassMetadata createMetadata(Class<?> clazz) {
		ClassMetadata metadata = new ClassMetadata();
		metadata.setPersistentClass(clazz);
		String kind = getKind(clazz);
		if (kind.length() > maxKindLength) {
			throw new ConfigException(kind + " is a long name for an entity kind. Consider using @Entity to make it shorter, which will save space in the Datastore. Use " + 
					getClass().getSimpleName() + ".setMaxKindLength() to disable this error");
		}
		metadata.setKind(kind);
		visit(clazz, metadata, new HashSet<String>());
		initParents(metadata);
		return metadata;
	}
	
	private void initParents(ClassMetadata metadata) {
		Id idAnn = metadata.getKeyProperty() != null? metadata.getKeyProperty().getAnnotation(Id.class) : null;
		Class<?>[] cparents = idAnn != null? idAnn.parent() : ROOT_ANCESTORS;
		if (cparents.length > 0) {
			Set<String> parents = Sets.newTreeSet();
			for (Class<?> clazz : cparents) {
				parents.add(getKind(clazz));
			}
			metadata.setParents(parents);
		}
	}
	
	private String getKind(Class clazz) {
		org.simpleds.annotations.Entity entity = (Entity) clazz.getAnnotation(org.simpleds.annotations.Entity.class);
		if (entity != null && entity.value().length() > 0) {
			return entity.value();
		}
		return clazz.getSimpleName().intern();
	}

	private void visit(Class<?> clazz, ClassMetadata classMetadata, Set<String> visitedPropertyNames) {
		String name = null;
		try {
			if (clazz == null || Object.class == clazz) {
				return;
			}
			
			// process @MultivaluedIndex
			if (clazz.getAnnotation(MultivaluedIndexes.class) != null) {
				for (MultivaluedIndex index : clazz.getAnnotation(MultivaluedIndexes.class).value()) {
					addMultivaluedIndex(classMetadata, index);
				}
			}
			if (clazz.getAnnotation(MultivaluedIndex.class) != null) {
				addMultivaluedIndex(classMetadata, clazz.getAnnotation(MultivaluedIndex.class));
			}
			
			// process @Cacheable
			if (clazz.getAnnotation(Cacheable.class) != null) {
				Cacheable cacheable = clazz.getAnnotation(Cacheable.class);
				classMetadata.setCacheSeconds(cacheable.value());
			}
			
			// add standard javabean properties
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			for (PropertyDescriptor property : descriptors) {
				name = property.getName();
				if (!visitedPropertyNames.contains(name) && isCandidate(property)) {
					visitedPropertyNames.add(name);
					addProperty(classMetadata, PropertyMetadataFactory.create(name, getDeclaredField(property), property.getReadMethod(), property.getWriteMethod()));
				}
			}
			
			// process attribute declarations
			for (Field field : clazz.getDeclaredFields()) {
				name = field.getName();
				if (!visitedPropertyNames.contains(name) && isCandidate(field)) {
					visitedPropertyNames.add(name);
					addProperty(classMetadata, PropertyMetadataFactory.create(name, field, null, null));
				}
			}
			
			// add superclass declarations
			visit(clazz.getSuperclass(), classMetadata, visitedPropertyNames);
			
		} catch (RuntimeException e) {
			throw new ConfigException("Could not process " + clazz.getSimpleName() + "." + name + ": " + e.getMessage(), e);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	private void addMultivaluedIndex(ClassMetadata classMetadata, MultivaluedIndex index) {
		MultivaluedIndexMetadata metadata = new MultivaluedIndexMetadata();
		metadata.setName(index.name());
		metadata.setKind(classMetadata.getKind() + "_" + index.name());
		metadata.setConverter(converterFactory.getCollectionConverter((Class<? extends Iterable>) index.collectionClass(), index.itemClass()));
		metadata.setClassMetadata(classMetadata);
		classMetadata.add(metadata);
	}

	private <J, D> void addProperty(ClassMetadata classMetadata, SinglePropertyMetadata<J, D> propertyMetadata) {
		if (propertyMetadata.getAnnotation(org.simpleds.annotations.Embedded.class) != null) {
			addEmbeddedProperties(classMetadata, propertyMetadata);
		} else {
			if (propertyMetadata.getConverter() == null) { // calculate default converter
				Converter<J, D> converter = converterFactory.getConverter(propertyMetadata);
				propertyMetadata.setConverter(converter);
			}
			if (propertyMetadata.getAnnotation(Version.class) != null) {
				AbstractVersionManager versionManager = null;
				Class<J> propertyType = propertyMetadata.getPropertyType();
				if (propertyType == Long.class || propertyType == Long.TYPE) {
					versionManager = new LongVersionManager();
				} else if (propertyType == Date.class) {
					versionManager = new DateVersionManager();
				} else {
					throw new IllegalArgumentException("@Version attribute " + classMetadata.getPersistentClass().getSimpleName() + "." + propertyMetadata.getName() + " is of unrecognized type " + propertyType.getName() + "; valid values are Long, long or Date");
				}
				versionManager.setPropertyMetadata(propertyMetadata);
				classMetadata.setVersionManager(versionManager);
			}
			doAddProperty(classMetadata, propertyMetadata);
		}
	}

	/**
	 * Recursively processes all nested {@link Embedded} properties and adds them as {@link EmbeddedPropertyMetadata} instances
	 */
	private void addEmbeddedProperties(ClassMetadata classMetadata, SinglePropertyMetadata propertyMetadata) {
		ClassMetadata nested = new ClassMetadata();
		nested.setPersistentClass(propertyMetadata.getPropertyType());
		visit(nested.getPersistentClass(), nested, new HashSet<String>());
		for (Iterator<String> i = nested.getPropertyNames(); i.hasNext(); ) {
			String propertyName = i.next();
			EmbeddedPropertyMetadata property = new EmbeddedPropertyMetadata(propertyMetadata, nested.getProperty(propertyName));
			doAddProperty(classMetadata, property);
		}
	}
	
	private void doAddProperty(ClassMetadata classMetadata, PropertyMetadata property) {
		if (property.getName().length() > maxPropertyLength) {
			throw new ConfigException(classMetadata.getPersistentClass().getSimpleName() + 
					"." + property.getName() + " is a long name for a property. Consider using @Property to make it shorter, which will save space in the Datastore. Use " + 
					getClass().getSimpleName() + ".setMaxPropertyLength() to disable this warning");
		}
		classMetadata.add(property);
	}

	/**
	 * Return true if the field is static or marked as transient
	 */
	private boolean isCandidate(Field field) {
		return !Modifier.isStatic(field.getModifiers()) && field.getAnnotation(Transient.class) == null;
	}

	private Field getDeclaredField(PropertyDescriptor property) {
		try {
			return property.getReadMethod().getDeclaringClass().getDeclaredField(property.getName());
		} catch (NoSuchFieldException e) {
			return null;
		}
	}
	
	/**
	 * Return true if the field is private or marked as transient
	 */
	private boolean isCandidate(PropertyDescriptor property) {
		if ("class".equals(property.getName()) || property.getReadMethod() == null || Modifier.isStatic(property.getReadMethod().getModifiers())) {
			return false;
		}
		Field field = getDeclaredField(property);
		if (field == null && property.getWriteMethod() == null) {
			log.debug("Property " + property.getName() + " has no setter method or private attribute. Skipping.");
			return false;
		}
		return property.getReadMethod().getAnnotation(Transient.class) == null && (field == null || field.getAnnotation(Transient.class) == null);  
	}

	/**
	 * Set maximum number of chars for an entity kind. Set to Integer.MAX_VALUE to disable the warning associated to long entity names
	 */
	public void setMaxKindLength(int maxKindChars) {
		this.maxKindLength = maxKindChars;
	}

	public void setConverterFactory(ConverterFactory converterFactory) {
		this.converterFactory = converterFactory;
	}

	public void setMaxPropertyLength(int maxPropertyLength) {
		this.maxPropertyLength = maxPropertyLength;
	}
	
}
