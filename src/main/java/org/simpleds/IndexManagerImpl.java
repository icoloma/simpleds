package org.simpleds;

import java.util.Collection;
import java.util.List;

import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.MultivaluedIndexMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.Lists;

public class IndexManagerImpl implements IndexManager {

	@Autowired 
	private DatastoreService datastoreService;
	
	@Autowired 
	private PersistenceMetadataRepository repository; 
	
	/** true to validate schema constraints, default true */
	private boolean enforceSchemaConstraints = true;

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Collection> T get(Key entityKey, String indexName) {
		MultivaluedIndexMetadata indexMetadata = getIndexMetadata(entityKey.getKind(), indexName);
		try {
			Entity entity = datastoreService.get(indexMetadata.createIndexKey(entityKey));
			Object result = entity.getProperty("contents");
			return (T) (result == null? indexMetadata.createEmptyIndex() : indexMetadata.getConverter().datastoreToJava(result));
		} catch (EntityNotFoundException e) {
			return (T) indexMetadata.createEmptyIndex();
		}
	}
	
	@Override
	public <T extends Collection> T addIndexValue(Key entityKey, String indexName, Object indexValue) {
		if (enforceSchemaConstraints) {
			MultivaluedIndexMetadata indexMetadata = getIndexMetadata(entityKey.getKind(), indexName);
			indexMetadata.validateIndexValue(indexValue);
		}
		
		T indexValues = (T) get(entityKey, indexName);
		indexValues.add(indexValue);
		put(entityKey, indexName, indexValues);
		return indexValues;
	}
	
	@Override
	public <T extends Collection> T deleteIndexValue(Key entityKey, String indexName, Object indexValue) {
		if (enforceSchemaConstraints) {
			MultivaluedIndexMetadata indexMetadata = getIndexMetadata(entityKey.getKind(), indexName);
			indexMetadata.validateIndexValue(indexValue);
		}
		
		T indexValues = (T) get(entityKey, indexName);
		indexValues.remove(indexValue);
		put(entityKey, indexName, indexValues);
		return indexValues;
	}

	@Override
	public void put(Key entityKey, String indexName, Collection indexValue) {
		MultivaluedIndexMetadata indexMetadata = getIndexMetadata(entityKey.getKind(), indexName);
		if (enforceSchemaConstraints && !indexValue.isEmpty()) {
			// validate only the first index value (suppose generics guarantee that all items share the same class)
			indexMetadata.validateIndexValue(indexValue.iterator().next());
		}
		
		Entity entity = new Entity(indexMetadata.createIndexKey(entityKey));
		entity.setProperty("contents", indexValue);
		datastoreService.put(entity);
	}
	
	public MultivaluedIndexMetadata getIndexMetadata(Class clazz, String indexName) {
		ClassMetadata metadata = repository.get(clazz);
		return metadata.getMultivaluedIndex(indexName);
	}

	public MultivaluedIndexMetadata getIndexMetadata(String kind, String indexName) {
		ClassMetadata metadata = repository.get(kind);
		return metadata.getMultivaluedIndex(indexName);
	}
	
	@Override
	public IndexQuery newQuery(Class entityClazz, String indexName) {
		MultivaluedIndexMetadata index = getIndexMetadata(entityClazz, indexName);
		return new IndexQuery(index);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> find(IndexQuery factory) {
		Key parentKey = null;
		try {
			Query query = factory.getQuery();
			ClassMetadata metadata = factory.getMetadata().getClassMetadata();
			
			PreparedQuery preparedQuery = datastoreService.prepare(query);
			FetchOptions fetchOptions = factory.getFetchOptions();
			Iterable<Entity> indexKeys = fetchOptions == null? preparedQuery.asIterable() : preparedQuery.asIterable(fetchOptions);
			List result = Lists.newArrayList();
			for (Entity entity : indexKeys) {
				parentKey = entity.getKey().getParent();
				result.add(factory.isKeysOnly()? parentKey : (T)metadata.datastoreToJava(datastoreService.get(parentKey)));
			}
			return result;
		} catch (EntityNotFoundException e) {
			// Should not happen. It means that the containing entity has been removed, but the index itself has not
			// TODO: log or rethrow?
			throw new org.simpleds.exception.EntityNotFoundException("Container entity with key " + parentKey + " could not be found");
		}
	}

	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public void setRepository(PersistenceMetadataRepository repository) {
		this.repository = repository;
	}

	public void setEnforceSchemaConstraints(boolean validateSchemaConstraints) {
		this.enforceSchemaConstraints = validateSchemaConstraints;
	}
}
