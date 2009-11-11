package org.simpleds.metadata;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.simpleds.exception.RequiredFieldException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortPredicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ClassMetadata {

	/** persistent class */
	private Class persistentClass;
	
	/** datastore kind */
	private String kind;
	
	/** primary key property accessor */
	private PropertyMetadata keyProperty;
	
	/** not null if the key value should be generated automatically, null otherwise */
	private boolean generateKeyValue;
	
	/** persistent properties */
	private Map<String, PropertyMetadata> properties = Maps.newHashMap();
	
	/** required properties */
	private Set<String> requiredProperties = Sets.newHashSet();
	
	/** relation indexes */
	private Map<String, MultivaluedIndexMetadata> multivaluedIndexes = Maps.newHashMap();
	
	/**
	 * Convert a value from Google representation to a Java value
	 * @param entity the persistent {@link Entity} from the google datastore
	 */
	@SuppressWarnings("unchecked")
	public <T> T datastoreToJava(Entity entity) {
		try {
			T result = (T) persistentClass.newInstance();
			for (Entry<String, Object> property : entity.getProperties().entrySet()) {
				PropertyMetadata metadata = properties.get(property.getKey());
				Object value = metadata.getConverter().datastoreToJava(property.getValue());
				metadata.setValue(result, value);
			}
			keyProperty.setValue(result, entity.getKey());
			return result;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Validate the schema constraints on the provided Entity instance
	 * @param entity the entity to validate
	 */
	public void validateConstraints(Entity entity) {
		for (String propertyName : requiredProperties) {
			if (entity.getProperty(propertyName) == null) {
				throw new RequiredFieldException("Required property '" + kind + "." + propertyName + "' is not set");
			}
		}
	}

	/**
	 * Validate the schema constraints on the provided Query instance
	 * @param query the query to validate
	 */
	public void validateConstraints(Query query) {
		if (!kind.equals(query.getKind())) {
			throw new IllegalArgumentException("The provided query kind '" + query.getKind() + "' does not match the expected kind '" + kind + "'");
		}
		
		for (FilterPredicate predicate : query.getFilterPredicates()) {
			String propertyName = predicate.getPropertyName();
			Class<?> expectedClass;
			if (!"__key__".equals(propertyName)) {
				PropertyMetadata propertyMetadata = getProperty(propertyName);
				expectedClass = propertyMetadata.getPropertyType();
			} else {
				expectedClass = Key.class;
			}
			Object value = predicate.getValue();
			if (value != null && !expectedClass.isAssignableFrom(value.getClass())) {
				throw new IllegalArgumentException("Value of " + propertyName + " of type " + value.getClass().getSimpleName() + 
						" cannot be converted to " + expectedClass.getSimpleName());
			}
		}
		for (SortPredicate predicate : query.getSortPredicates()) {
			String propertyName = predicate.getPropertyName();
			if (!"__key__".equals(propertyName) && !properties.containsKey(propertyName)) {
				throwPropertyNotFoundException(propertyName);
			}
		}

	}
	/**
	 * Convert a value from Java representation to a Datastore {@link Entity}
	 * @param javaObject the Java property value
	 * @param parentKey the parent {@link Key} (may be null)
	 */
	@SuppressWarnings("unchecked")
	public Entity javaToDatastore(Key parentKey, Object javaObject) {
		String kind = javaObject.getClass().getSimpleName();
		Key key = (Key) keyProperty.getValue(javaObject);
		Entity entity = key == null? new Entity(kind, parentKey) : new Entity(key); 
		for (Entry<String, PropertyMetadata> property : properties.entrySet()) {
			String name = property.getKey();
			PropertyMetadata metadata = property.getValue();
			Object value = metadata.getConverter().javaToDatastore(metadata.getValue(javaObject));
			if (value != null) { // null values are omitted
				entity.setProperty(name, value);
			}
		}
		return entity;
	}
	
	public void validate() {
		if (keyProperty == null) {
			throw new IllegalArgumentException("No key property specified for persistent class " + persistentClass.getSimpleName());
		}	
	} 
	
	public void add(PropertyMetadata property) {
		if (property.getAnnotation(Id.class) != null) {
			if (keyProperty != null) {
				throw new IllegalArgumentException("Key property specified more than once for class " + persistentClass.getSimpleName() + "(" + keyProperty.getName() + ", " + property.getName() + ")");
			}
			keyProperty = property;
			generateKeyValue = property.getAnnotation(GeneratedValue.class) != null;
			Class type = property.getPropertyType();
			if (!Key.class.equals(type)) {
				throw new IllegalArgumentException("Error processing " + persistentClass.getSimpleName() + ". Only Key.class is supported as primary key");
			}
		} else {
			Basic basic = property.getAnnotation(Basic.class);
			Column column = property.getAnnotation(Column.class);
			if (basic != null && !basic.optional() || column != null && !column.nullable()) {
				requiredProperties.add(property.getName());
			}
			if (properties.keySet().contains(property.getName())) {
				throw new IllegalArgumentException("Attempting to add twice a property with name '" + property.getName() + 
						"' for class " + persistentClass.getSimpleName() + 
						". Existing: " + properties.get(property.getName()) + 
						", New: " + property);
			}
			properties.put(property.getName(), property);
		}
	}
	
	/**
	 * @return true if this metadata instance contains a property or key with the provided name
	 */
	public boolean contains(String propertyName) {
		return  properties.keySet().contains(propertyName) || (keyProperty != null && keyProperty.getName().equals(propertyName));
	}
	
	public Class getPersistentClass() {
		return persistentClass;
	}

	public void setPersistentClass(Class persistentClass) {
		this.persistentClass = persistentClass;
		this.kind = persistentClass.getSimpleName();
	}

	public PropertyMetadata getProperty(String propertyName) {
		PropertyMetadata metadata = properties.get(propertyName);
		if (metadata == null) {
			throwPropertyNotFoundException(propertyName);
		}
		return metadata;
	}
	
	private void throwPropertyNotFoundException(String propertyName) {
		throw new IllegalArgumentException("Persistent property " + kind + "." + propertyName + " not found");
	}
	
	public MultivaluedIndexMetadata getMultivaluedIndex(String relationIndexName) {
		MultivaluedIndexMetadata index = multivaluedIndexes.get(relationIndexName);
		if (index == null) {
			throw new IllegalArgumentException("MultivaluedIndex with name '" + relationIndexName + "' cannot be found");
		}
		return index;
	}
	
	public void add(MultivaluedIndexMetadata metadata) {
		multivaluedIndexes.put(metadata.getName(), metadata);
	}
	
	public Iterator<String> getPropertyNames() {
		return properties.keySet().iterator();
	}

	public void setProperties(Map<String, PropertyMetadata> properties) {
		this.properties = properties;
	}
	public boolean isGenerateKeyValue() {
		return generateKeyValue;
	}
	public void setGenerateKeyValue(boolean generateKey) {
		this.generateKeyValue = generateKey;
	}

	public PropertyMetadata getKeyProperty() {
		return keyProperty;
	}

	public Set<String> getRequiredProperties() {
		return requiredProperties;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}


}
