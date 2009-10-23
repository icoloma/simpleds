package org.simpleds;

import java.util.Collection;

import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.MultivaluedIndexMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

public class IndexManagerImpl implements IndexManager {

	@Autowired 
	private DatastoreService datastoreService;
	
	@Autowired 
	private PersistenceMetadataRepository repository; 

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Collection> T get(Key entityKey, String indexName) {
		ClassMetadata metadata = repository.get(entityKey.getKind());
		MultivaluedIndexMetadata relationIndex = metadata.getMultivaluedIndex(indexName);
		try {
			Entity entity = datastoreService.get(relationIndex.createIndexKey(entityKey));
			return (T) entity.getProperty("contents");
		} catch (EntityNotFoundException e) {
			return (T) relationIndex.createEmptyIndex();
		}
	}
	
	@Override
	public <T extends Collection> T addIndexValue(Key entityKey, String indexName, Object indexValue) {
		T indexValues = (T) get(entityKey, indexName);
		indexValues.add(indexValue);
		put(entityKey, indexName, indexValues);
		return indexValues;
	}
	
	@Override
	public <T extends Collection> T deleteIndexValue(Key entityKey, String indexName, Object indexValue) {
		T indexValues = (T) get(entityKey, indexName);
		indexValues.remove(indexValue);
		put(entityKey, indexName, indexValues);
		return indexValues;
	}

	@Override
	public void put(Key entityKey, String indexName, Collection indexValue) {
		ClassMetadata metadata = repository.get(entityKey.getKind());
		MultivaluedIndexMetadata relationIndex = metadata.getMultivaluedIndex(indexName);
		Entity entity = new Entity(relationIndex.createIndexKey(entityKey));
		entity.setProperty("contents", indexValue);
		datastoreService.put(entity);
	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setRepository(PersistenceMetadataRepository repository) {
		this.repository = repository;
	}
}
