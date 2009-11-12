package org.simpleds.metadata;

import java.util.Collection;

import org.simpleds.converter.CollectionConverter;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** 
 * Contains the information of a 1-to-many relationship handled as a especific property
 * stored in a separate entity.
 * Relation Index has been introduced here: http://www.youtube.com/watch?v=AgaL6NGpkB8 
 * @author icoloma
 */
public class MultivaluedIndexMetadata {

	/** the name of this index */
	private String name;
	
	/** the kind to be used when allocating keys */
	private String kind;
	
	/** the Converter to transform the entire collection*/
	private CollectionConverter converter;

	/** the containing class */
	private ClassMetadata classMetadata;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Create an index key associated to the provided persistent entity key
	 */
	public Key createIndexKey(Key entityKey) {
		return KeyFactory.createKey(entityKey, kind, 1L);
	}

	/**
	 * Creates an empty collection index
	 */
	@SuppressWarnings("unchecked")
	public <T extends Collection> T createEmptyIndex() {
		return (T) converter.createCollection(10);
	}

	public void setConverter(CollectionConverter converter) {
		this.converter = converter;
	}

	public CollectionConverter getConverter() {
		return converter;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	public void setClassMetadata(ClassMetadata classMetadata) {
		this.classMetadata = classMetadata;
	}

	/**
	 * Validate a value to be added/removed from this index
	 * @throws IllegalArgumentException if the index value is not valid
	 */
	public void validateIndexValue(Object indexValue) {
		if (!converter.getItemType().isAssignableFrom(indexValue.getClass())) {
			throw new IllegalArgumentException("Value " + indexValue + " is not of a valid type. Expected " + converter.getItemType().getSimpleName() + ", found " + indexValue.getClass().getSimpleName());
		}
	}
	
}
