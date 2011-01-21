package org.simpleds.metadata;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.simpleds.annotations.Property;
import org.simpleds.annotations.Transient;
import org.simpleds.exception.ConfigException;
import org.simpleds.exception.RequiredFieldException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.Expiration;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ClassMetadata {

	/** persistent class */
	private Class<?> persistentClass;
	
	/** datastore kind */
	private String kind;
	
	/** primary key property */
	private PropertyMetadata<Key, Key> keyProperty;
	
	/** not null if the key value should be generated automatically, null otherwise */
	private boolean generateKeyValue;
	
	/** the expected parent kind names, empty if this class is a root class (default empty) */
	private Set<String> parents = ImmutableSet.of();
	
	/** persistent properties */
	private Map<String, PropertyMetadata<?, ?>> properties = Maps.newHashMap();
	
	/** required properties */
	private Set<String> requiredProperties = Sets.newHashSet();
	
	/** relation indexes */
	private Map<String, MultivaluedIndexMetadata> multivaluedIndexes = Maps.newHashMap();
	
	/** the number of seconds that this class can be cached in memcache */
	private Integer cacheSeconds;
	
	/** manages a ny Version attribute. May be null */
	private VersionManager versionManager;
	
	private static Logger log = LoggerFactory.getLogger(ClassMetadata.class);
	
	/**
	 * Convert a value from Google representation to a Java value
	 * @param entity the persistent {@link Entity} from the google datastore
	 */
	@SuppressWarnings("unchecked")
	public <T> T datastoreToJava(Entity entity) {
		try {
			if (entity == null) {
				return null;
			}
			T result = (T) persistentClass.newInstance();
			populate(entity, result);
			return result;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copy all properties from the datastore entity into the persistent class instance passed as an attribute.
	 * @param from the entity read from the datastore
	 * @param to the java object to populate. Cannot be null.
	 */
	public void populate(Entity from, Object to) {
		for (Entry<String, Object> property : from.getProperties().entrySet()) {
			PropertyMetadata metadata = properties.get(property.getKey());
			if (metadata != null) {
				Object value = metadata.getConverter().datastoreToJava(property.getValue());
				metadata.setValue(to, value);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Unmapped attribute found in DataStore entry. Ignoring: " + from.getKind() + "." + property.getKey());
				} 
			}
		}
		keyProperty.setValue(to, from.getKey());
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
	 * Convert a value from Java representation to a Datastore {@link Entity}
	 * @param javaObject the Java property value
	 * @param parentKey the parent {@link Key} (may be null)
	 */
	public Entity javaToDatastore(Key parentKey, Object javaObject) {
		String kind = javaObject.getClass().getSimpleName();
		Key key = keyProperty.getValue(javaObject);
		Entity entity = key == null? new Entity(kind, parentKey) : new Entity(key); 
		for (Entry<String, PropertyMetadata<?, ?>> entry : properties.entrySet()) {
			PropertyMetadata property = entry.getValue();
			String name = entry.getKey();
			Object javaValue = property.getValue(javaObject);
			property.setEntityValue(entity, javaValue);
		}
		return entity;
	}
	
	public void validate() {
		if (keyProperty == null) {
			throw new IllegalArgumentException("No key property specified for persistent class " + persistentClass.getSimpleName());
		}	
	} 
	
	@SuppressWarnings("unchecked")
	public void add(PropertyMetadata<?, ?> property) {
		org.simpleds.annotations.Id simpledsId = property.getAnnotation(org.simpleds.annotations.Id.class);
		if (property.getAnnotation(Id.class) != null || simpledsId != null) {
			if (keyProperty != null) {
				throw new IllegalArgumentException("Key property specified more than once for class " + persistentClass.getSimpleName() + "(" + keyProperty.getName() + ", " + property.getName() + ")");
			}
			keyProperty = (PropertyMetadata<Key, Key>) property;
			generateKeyValue = property.getAnnotation(GeneratedValue.class) != null || (simpledsId != null && simpledsId.generated());
			Class<?> type = property.getPropertyType();
			if (!Key.class.equals(type)) {
				throw new IllegalArgumentException("Error processing " + persistentClass.getSimpleName() + ". Only Key.class is supported as primary key");
			}
		} else if (property.getAnnotation(Transient.class) == null){
			Basic basic = property.getAnnotation(Basic.class);
			Column column = property.getAnnotation(Column.class);
			Property propertyAnn = property.getAnnotation(Property.class); 
			if (basic != null && !basic.optional() || column != null && !column.nullable() || propertyAnn != null && propertyAnn.required()) {
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
	
	public Class<?> getPersistentClass() {
		return persistentClass;
	}

	public void setPersistentClass(Class<?> persistentClass) {
		this.persistentClass = persistentClass;
		this.kind = persistentClass.getSimpleName().intern();
	}

	@SuppressWarnings("unchecked")
	public <J, D> PropertyMetadata<J, D> getProperty(String propertyName) {
		PropertyMetadata<J, D> metadata = (PropertyMetadata<J, D>) properties.get(propertyName);
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

	/**
	 * Validates the parent key when inserting
	 */
	public void validateParentKey(Key parentKey) {
		if (parents.isEmpty()) {
			if (parentKey != null) {
				throw new IllegalArgumentException("Specified parent key " + parentKey + ", but entity " + this.kind + " is configured as a root class (missing @Id(parent)?)");
			}
		} else {
			if (parentKey == null) {
				throw new IllegalArgumentException("Missing parent key for entity " + this.kind + ". Expected: " + Joiner.on(", ").join(parents));
			}
			if (!parents.contains(parentKey.getKind())) {
				throw new IllegalArgumentException("Specified parent key " + parentKey + ", but entity " + this.kind + " expects parents with type " + Joiner.on(", ").join(parents));
			}
		}
	}
	
	/**
	 * @return true if this class is cacheable
	 */
	public boolean isCacheable() {
		return cacheSeconds != null;
	}
	
	public void add(MultivaluedIndexMetadata metadata) {
		multivaluedIndexes.put(metadata.getName(), metadata);
	}
	
	public Iterator<String> getPropertyNames() {
		return properties.keySet().iterator();
	}

	public void setProperties(Map<String, PropertyMetadata<?, ?>> properties) {
		this.properties = properties;
	}
	public boolean isGenerateKeyValue() {
		return generateKeyValue;
	}
	public void setGenerateKeyValue(boolean generateKey) {
		this.generateKeyValue = generateKey;
	}

	public PropertyMetadata<Key, Key> getKeyProperty() {
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

	public void setParents(Set<String> parents) {
		this.parents = parents;
	}

	public Set<String> getParents() {
		return parents;
	}

	public int getCacheSeconds() {
		return cacheSeconds;
	}

	public void setCacheSeconds(int cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
	}

	/**
	 * Create a Expiration instance to be used with memcache 
	 */
	public Expiration createCacheExpiration() {
		return Expiration.byDeltaSeconds(cacheSeconds);
	}

	public boolean useLevel2Cache() {
		return cacheSeconds > 0;
	}

	public void setVersionManager(VersionManager versionManager) {
		if (this.versionManager != null) {
			throw new ConfigException("@Version specified twice for " + persistentClass.getName());
		}
		this.versionManager = versionManager;
	}

	public VersionManager getVersionManager() {
		return versionManager;
	}

}
