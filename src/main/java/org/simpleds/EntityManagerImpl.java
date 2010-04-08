package org.simpleds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PropertyMetadata;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class EntityManagerImpl implements EntityManager {

	private DatastoreService datastoreService;
	
	private PersistenceMetadataRepository repository; 
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	@Override
	public Transaction beginTransaction() {
		return datastoreService.beginTransaction();
	}
	
	@Override
	public ClassMetadata getClassMetadata(Class<?> clazz) {
		return repository.get(clazz);
	}
	
	@Override
	public ClassMetadata getClassMetadata(String kind) {
		return repository.get(kind);
	}
	
	@Override
	public SimpleQuery createQuery(String kind) {
		return createQueryImpl(null, repository.get(kind));
	}
	
	@Override
	public SimpleQuery createQuery(Class<?> clazz) {
		return createQueryImpl(null, repository.get(clazz));
	}
	
	@Override
	public SimpleQuery createQuery(Key ancestor, String kind) {
		return createQueryImpl(ancestor, repository.get(kind));
	}
	
	@Override
	public SimpleQuery createQuery(Key ancestor, Class<?> clazz) {
		return createQueryImpl(ancestor, repository.get(clazz));
	}
	
	@Override
	public PagedQuery createPagedQuery(String kind) {
		return createPagedQueryImpl(null, repository.get(kind));
	}
	
	@Override
	public PagedQuery createPagedQuery(Class<?> clazz) {
		return createPagedQueryImpl(null, repository.get(clazz));
	}
	
	@Override
	public PagedQuery createPagedQuery(Key ancestor, String kind) {
		return createPagedQueryImpl(ancestor, repository.get(kind));
	}
	
	@Override
	public PagedQuery createPagedQuery(Key ancestor, Class<?> clazz) {
		return createPagedQueryImpl(ancestor, repository.get(clazz));
	}
	
	private PagedQuery createPagedQueryImpl(Key ancestor, ClassMetadata metadata) {
		if (enforceSchemaConstraints && ancestor != null) { 
			metadata.validateParentKey(ancestor);
		}
		return new PagedQuery(ancestor, metadata);
	}
	
	private SimpleQuery createQueryImpl(Key ancestor, ClassMetadata metadata) {
		if (enforceSchemaConstraints && ancestor != null) { 
			metadata.validateParentKey(ancestor);
		}
		return new SimpleQuery(ancestor, metadata);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> find(SimpleQuery simpleQuery) {
		List result = Lists.newArrayList();
		SimpleQueryResultIterable<T> iterable = asIterable(simpleQuery);
		for (T item : iterable) {
			result.add(item);
		}
		return result;
	}
	
	@Override
	public <T> SimpleQueryResultIterable<T> asIterable(SimpleQuery simpleQuery) {
		Query query = simpleQuery.getQuery();
		ClassMetadata metadata = simpleQuery.getClassMetadata();
		PreparedQuery preparedQuery = datastoreService.prepare(simpleQuery.getTransaction(), query);
		FetchOptions fetchOptions = simpleQuery.getFetchOptions();
		QueryResultIterable<Entity> iterable = fetchOptions == null? preparedQuery.asQueryResultIterable() : preparedQuery.asQueryResultIterable(fetchOptions);
		return new SimpleQueryResultIterableImpl<T>(metadata, iterable).setKeysOnly(simpleQuery.isKeysOnly());
	}
	
	@Override
	public <T> SimpleQueryResultIterator<T> asIterator(SimpleQuery simpleQuery) {
		SimpleQueryResultIterable<T> iterable = asIterable(simpleQuery);
		return iterable.iterator();
	}
	
	@Override
	public <T> List<T> findChildren(Key parentKey, Class<T> childrenClass) {
		return find(createQuery(parentKey, childrenClass));
	}
	
	@Override
	public List<Key> findChildrenKeys(Key parentKey, Class childrenClass) {
		return find(createQuery(parentKey, childrenClass).keysOnly());
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
		if (!q.isKeysOnly()) {
			q = q.clone().keysOnly();
		}
		return datastoreService.prepare(q.getQuery()).countEntities();
	}
	
	@Override
	public Key put(Object javaObject) {
		return put(null, null, javaObject);
	}
	
	@Override
	public Key put(Transaction transaction, Object javaObject) {
		return put(transaction, null, javaObject);
	}
	
	@Override
	public Key put(Key parentKey, Object javaObject) {
		return this.put(null, parentKey, javaObject);
	}
	
	@Override
	public Key put(Transaction transaction, Key parentKey, Object javaObject) {
		ClassMetadata metadata = repository.get(javaObject.getClass());
		
		// generate primary key if missing
		PropertyMetadata keyProperty = metadata.getKeyProperty();
		Key providedKey = (Key) keyProperty.getValue(javaObject);
		if (providedKey == null && !metadata.isGenerateKeyValue()) {
			throw new IllegalArgumentException("No key value provided for " + javaObject.getClass().getSimpleName() + " instance, but key generation is not enabled for this class (missing @GeneratedValue?)");
		}
		
		if (enforceSchemaConstraints) {
			metadata.validateParentKey(providedKey == null? parentKey : providedKey.getParent());
		}
		
		// transform to entity instance
		Entity entity = metadata.javaToDatastore(parentKey, javaObject);
		
		// check required fields
		if (enforceSchemaConstraints) {
			metadata.validateConstraints(entity);
		}
		
		// persist and set the returned primary key value
		Key newKey = datastoreService.put(transaction, entity);
		if (providedKey == null) {
			keyProperty.setValue(javaObject, newKey);
		}
		return newKey;
	}
	
	@Override
	public void put(Collection javaObjects) {
		put(null, null, javaObjects);
	}
	
	@Override
	public void put(Transaction transaction, Collection javaObjects) {
		put(transaction, null, javaObjects);
	}
	
	@Override
	public void put(Key parentKey, Collection javaObjects) {
		put(null, parentKey, javaObjects);
	}
	
	@Override
	public void put(Transaction transaction, Key parentKey, Collection javaObjects) {
		
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
			if (enforceSchemaConstraints) {
				metadata.validateParentKey(parentKey);
			}
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
		datastoreService.put(transaction, entities);
	}
	
	@Override
	public void delete(Key... keys) {
		delete(null, keys);
	}
	
	@Override
	public void delete(Transaction transaction, Key... keys) {
		datastoreService.delete(transaction, keys);
	}
	
	@Override
	public void delete(Iterable<Key> keys) {
		delete(null, keys);
	}
	
	@Override
	public void delete(Transaction transaction, Iterable<Key> keys) {
		datastoreService.delete(transaction, keys);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Key key) {
		return (T) get(null, key);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Transaction transaction, Key key) {
		try {
			Entity entity = datastoreService.get(transaction, key);
			return (T) repository.get(key.getKind()).datastoreToJava(entity);
		} catch (EntityNotFoundException e) {
			throw new org.simpleds.exception.EntityNotFoundException(e);
		}
	}
	
	@Override
	public <T> List<T> get(Iterable<Key> keys) {
		return get(null, keys);
	}
	
	@Override
	public <T> List<T> get(Transaction transaction, Iterable<Key> keys) {
		Map<Key, Entity> entities = datastoreService.get(transaction, keys);
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
