package org.simpleds.metadata;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

/**
 * Stores all the metadata about persistence strategy for each class
 * @author icoloma
 */
public class PersistenceMetadataRepository {

	/** the list of ClassMetadata by class */
	private Map<Class, ClassMetadata> metadataByClass = Maps.newHashMap();
	
	/** the list of ClassMetadata by simple (unqualified) class name */
	private Map<String, ClassMetadata> metadataBySimpleClassName = Maps.newHashMap();
	
	private static Log log = LogFactory.getLog(PersistenceMetadataRepository.class);
	
	public void add(ClassMetadata metadata) {
		log.debug("Adding persistent class " + metadata.getKind());
		metadata.validate();
		metadataByClass.put(metadata.getPersistentClass(), metadata);
		metadataBySimpleClassName.put(metadata.getKind(), metadata);
	}
	
	/**
	 * @return the ClassMetadata regsitered for the provided class 
	 */
	public ClassMetadata get(Class clazz) {
		ClassMetadata result = metadataByClass.get(clazz);
		if (result == null) {
			throw new IllegalArgumentException("No persistent class " + clazz.getName() + " could be found");
		}
		return result;
	}
	
	/**
	 * @param simpleName the simple (unqualified) class name
	 * @return the ClassMetadata regsitered for the provided simple class name
	 */
	public ClassMetadata get(String simpleName) {
		ClassMetadata result = metadataBySimpleClassName.get(simpleName);
		if (result == null) {
			throw new IllegalArgumentException("No persistent class " + simpleName + " could be found");
		}
		return result;
	}
	
	/**
	 * @return the list of all configured {@link ClassMetadata} instances
	 */
	public Collection<ClassMetadata> getAll() {
		return metadataByClass.values();
	}
	
}
