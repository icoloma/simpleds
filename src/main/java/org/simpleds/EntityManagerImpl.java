package org.simpleds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PropertyMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class EntityManagerImpl implements EntityManager {

	@Autowired 
	private DatastoreService datastoreService;
	
	@Autowired 
	private PersistenceMetadataRepository repository; 
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	@Override
	public Transaction beginTransaction() {
		return datastoreService.beginTransaction();
	}
	
	@Override
	public Transaction getCurrentTransaction() {
		return datastoreService.getCurrentTransaction();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> find(SimpleQuery factory) {
		Query query = factory.getQuery();
		
		// check that all constraints and sort properties belong to this schema
		ClassMetadata metadata = repository.get(query.getKind());
		if (enforceSchemaConstraints) {
			metadata.validateConstraints(query);
		}
		
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		FetchOptions fetchOptions = factory.getFetchOptions();
		Iterable<Entity> entities = fetchOptions == null? preparedQuery.asIterable() : preparedQuery.asIterable(fetchOptions);
		List result = Lists.newArrayList();
		for (Entity entity : entities) {
			result.add(query.isKeysOnly()? entity.getKey() : (T)metadata.datastoreToJava(entity));
		}
		return result;
	}
	
	@Override
	public <T> List<T> findChildren(Key parentKey, Class<T> childrenClass) {
		return find(new SimpleQuery(parentKey, childrenClass));
	}
	
	@Override
	public List<Key> findChildrenKeys(Key parentKey, Class childrenClass) {
		return find(new SimpleQuery(parentKey, childrenClass).keysOnly());
	}
	
	@Override
	public <T> T findSingle(SimpleQuery q) {
		Entity entity = datastoreService.prepare(q.getQuery()).asSingleEntity();
		if (entity == null) {
			throw new org.simpleds.exception.EntityNotFoundException();
		}
		return (T) datastoreToJava(entity);
	}
	
	@Override
	public int count(SimpleQuery q) {
		if (!q.isKeysOnly())
			q = q.clone().keysOnly();
		return datastoreService.prepare(q.getQuery()).countEntities();
	}
	
	@Override
	public Key put(Object javaObject) {
		return put(null, javaObject);
	}
	
	@Override
	public void put(Collection javaObjects) {
		put(null, javaObjects);
	}
	
	@Override
	public Key put(Key parentKey, Object javaObject) {
		ClassMetadata metadata = repository.get(javaObject.getClass());
		
		// generate primary key if missing
		PropertyMetadata keyProperty = metadata.getKeyProperty();
		Key providedKey = (Key) keyProperty.getValue(javaObject);
		if (providedKey == null && !metadata.isGenerateKeyValue()) {
			throw new IllegalArgumentException("No key value provided for " + javaObject.getClass().getSimpleName() + " instance, but key generation is not enabled for that class (missing @GeneratedValue?)");
		}
		
		// transform to entity instance
		Entity entity = metadata.javaToDatastore(parentKey, javaObject);
		
		// check required fields
		if (enforceSchemaConstraints) {
			metadata.validateConstraints(entity);
		}
		
		// persist and set the returned primary key value
		Key newKey = datastoreService.put(entity);
		if (providedKey == null) {
			keyProperty.setValue(javaObject, newKey);
		}
		return newKey;
	}
	
	@Override
	public void put(Key parentKey, Collection javaObjects) {
		
		Iterator it = javaObjects.iterator();
		if (!it.hasNext()) {
			return;
		}
		
		// allocate and set missing primary keys (in bulk)
		ListMultimap<Class, Object> transientInstances = ArrayListMultimap.create();
		for (Object javaObject : javaObjects) {
			Class<? extends Object> clazz = javaObject.getClass();
			ClassMetadata metadata = repository.get(clazz);
			Key key = (Key) metadata.getKeyProperty().getValue(javaObject);
			if (key == null) {
				if (!metadata.isGenerateKeyValue()) {
					throw new IllegalArgumentException("No key value provided for " + javaObject + ", but key generation is not enabled for " + metadata.getKind() + " (missing @GeneratedValue?)");
				}
				transientInstances.put(clazz, javaObject);
			}
		}
		for (Class clazz : transientInstances.keySet()) {
			List<Object> instances = transientInstances.get(clazz);
			ClassMetadata metadata = repository.get(clazz);
			Iterator<Key> allocatedKeys = datastoreService.allocateIds(parentKey, metadata.getKind(), instances.size()).iterator();
			for (Object javaObject : instances) {
				metadata.getKeyProperty().setValue(javaObject, allocatedKeys.next());
			}
		}
		
		// transform to entity instances and persist
		List<Entity> entities = new ArrayList<Entity>(javaObjects.size());
		for (Object javaObject : javaObjects) {
			ClassMetadata metadata = repository.get(javaObject.getClass()); 
			Entity entity = metadata.javaToDatastore(parentKey, javaObject);
			
			// check required fields
			if (enforceSchemaConstraints) {
				metadata.validateConstraints(entity);
			}
			
			entities.add(entity);
		}
		
		
		// persist 
		datastoreService.put(entities);
	}
	
	@Override
	public void delete(Key... keys) {
		datastoreService.delete(keys);
	}
	
	@Override
	public void delete(Iterable<Key> keys) {
		datastoreService.delete(keys);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Key key) {
		try {
			Entity entity = datastoreService.get(key);
			return (T) repository.get(key.getKind()).datastoreToJava(entity);
		} catch (EntityNotFoundException e) {
			throw new org.simpleds.exception.EntityNotFoundException(e);
		}
	}
	
	@Override
	public <T> List<T> get(Iterable<Key> keys) {
		Map<Key, Entity> entities = datastoreService.get(keys);
		List<T> result = Lists.newArrayList();
		for (Map.Entry<Key, Entity> entry : entities.entrySet()) {
			ClassMetadata metadata = repository.get(entry.getKey().getKind());
			T javaObject = (T) metadata.datastoreToJava(entry.getValue());
			result.add(javaObject);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T datastoreToJava(Entity entity) {
		ClassMetadata metadata = repository.get(entity.getKind());
		return (T)metadata.datastoreToJava(entity);
	}
	
	@Override
	public Entity javaToDatastore(Object javaObject) {
		ClassMetadata metadata = repository.get(javaObject.getClass());
		return metadata.javaToDatastore(null, javaObject);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> PagedList<T> findPaged(PagedQuery query) {
		PagedList pagedList = new PagedList<T>(query, (List<T>) find(query));
		if (query.calculateTotalResults()) {
			pagedList.setTotalResults(count(query));
		}
		return pagedList;
	}
	
	public void setDatastoreService(DatastoreService service) {
		this.datastoreService = service;
	}
	
	public void setRepository(PersistenceMetadataRepository repository) {
		this.repository = repository;
	}
	
	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}
		
}
