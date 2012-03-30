package org.simpleds.metadata;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.simpleds.exception.DuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Stores all the metadata about persistence strategy for each class
 * @author icoloma
 */
public class PersistenceMetadataRepository {

	/** the list of ClassMetadata by class */
	private Map<Class, ClassMetadata> metadataByClass = Maps.newHashMap();
	
	/** the list of ClassMetadata by simple (unqualified) class name */
	private Map<String, ClassMetadata> metadataByKind = Maps.newHashMap();
	
	@Inject
	private ClassMetadataFactory classMetadataFactory;

	private static Logger log = LoggerFactory.getLogger(PersistenceMetadataRepository.class);
	
	/**
	 * Adds a persistent class to the repository
	 * @return the ClassMetadata instance created for this persistent class
	 */
	public ClassMetadata add(Class<?> clazz) {
		ClassMetadata metadata = classMetadataFactory.createMetadata(clazz);
		log.debug("Adding persistent class " + metadata.getKind());
		metadata.validate();
		if (metadataByKind.get(metadata.getKind()) != null) {
			throw new DuplicateException("Two entities found with kind='" + metadata.getKind() + "': " + metadata.getPersistentClass().getName() + " and " + metadataByKind.get(metadata.getKind()).getPersistentClass().getName());
		}
		metadataByClass.put(metadata.getPersistentClass(), metadata);
		metadataByKind.put(metadata.getKind(), metadata);
		return metadata;
	}
	
	/**
	 * Removes a persistent class from the repository
	 * @param clazz the persistent class to remove
	 * @throws IllegalArgumentException if the class is not present in the repository
	 */
	public void remove(Class<?> clazz) {
		ClassMetadata metadata = get(clazz);
		metadataByClass.remove(clazz);
		metadataByKind.remove(metadata.getKind());
	}
	
	/**
	 * @return the ClassMetadata regsitered for the provided class 
	 * @throws IllegalArgumentException if the provided class is not registered as a persistent class
	 */
	public ClassMetadata get(Class clazz) {
		ClassMetadata result = metadataByClass.get(clazz);
		if (result == null) {
			throw new IllegalArgumentException("No persistent class " + clazz.getName() + " could be found");
		}
		return result;
	}
	
	/**
	 * @param kind the simple (unqualified) class name
	 * @return the ClassMetadata regsitered for the provided simple class name
	 */
	public ClassMetadata get(String kind) {
		ClassMetadata result = metadataByKind.get(kind);
		if (result == null) {
			throw new IllegalArgumentException("No persistent class " + kind + " could be found");
		}
		return result;
	}
	
	/**
	 * @return the list of all configured {@link ClassMetadata} instances
	 */
	public Collection<ClassMetadata> getAll() {
		return metadataByClass.values();
	}

	public void setClassMetadataFactory(ClassMetadataFactory classMetadataFactory) {
		this.classMetadataFactory = classMetadataFactory;
	}
	
}
