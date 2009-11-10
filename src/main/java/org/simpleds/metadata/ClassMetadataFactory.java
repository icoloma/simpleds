package org.simpleds.metadata;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.simpleds.annotations.MultivaluedIndex;
import org.simpleds.annotations.MultivaluedIndexes;
import org.simpleds.converter.ConverterFactory;
import org.simpleds.exception.ConfigException;

public class ClassMetadataFactory {

	private static Log log = LogFactory.getLog(ClassMetadataFactory.class);
	
	public ClassMetadata createMetadata(Class clazz) {
		ClassMetadata metadata = new ClassMetadata();
		metadata.setPersistentClass(clazz);
		visit(clazz, metadata, new HashSet<String>());
		return metadata;
	}
	
	private void visit(Class<?> clazz, ClassMetadata classMetadata, Set<String> visitedPropertyNames) {
		String name = null;
		try {
			if (clazz == null || Object.class == clazz) {
				return;
			}
			
			// process any RelationIndex
			if (clazz.getAnnotation(MultivaluedIndexes.class) != null) {
				for (MultivaluedIndex index : clazz.getAnnotation(MultivaluedIndexes.class).value()) {
					addMultivaluedIndex(classMetadata, index);
				}
			}
			if (clazz.getAnnotation(MultivaluedIndex.class) != null) {
				addMultivaluedIndex(classMetadata, clazz.getAnnotation(MultivaluedIndex.class));
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
		metadata.setConverter(ConverterFactory.getCollectionConverter(index.collectionClass(), index.itemClass()));
		metadata.setClassMetadata(classMetadata);
		classMetadata.add(metadata);
	}

	private void addProperty(ClassMetadata classMetadata, SinglePropertyMetadata propertyMetadata) {
		if (propertyMetadata.getAnnotation(OneToOne.class) != null || 
				propertyMetadata.getAnnotation(ManyToOne.class) != null ||
				propertyMetadata.getAnnotation(OneToMany.class) !=  null ||
				propertyMetadata.getAnnotation(ManyToMany.class) != null) {
			throw new IllegalArgumentException("Property " + classMetadata.getKind() + "." + propertyMetadata.getName() + " cannot be processed. The following annotations are not supported: @OneToOne, @ManyToOne, @OneToMany, @ManyToMany");
		}
		if (propertyMetadata.getAnnotation(Embedded.class) != null) {
			addEmbeddedProperties(classMetadata, propertyMetadata);
		} else {
			propertyMetadata.setConverter(ConverterFactory.getConverter(propertyMetadata));
			classMetadata.add(propertyMetadata);
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
			//SinglePropertyMetadata lastNode = property.getLastNode();
			//lastNode.setConverter(ConverterFactory.getConverter(lastNode));
			classMetadata.add(property);
		}
	}

	/**
	 * Return true if the field is marked as transient
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
}
