package org.simpleds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.simpleds.cache.CacheManager;
import org.simpleds.cache.NonCachedPredicate;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PropertyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class EntityManagerImpl implements EntityManager {

	private DatastoreService datastoreService;
	
	private PersistenceMetadataRepository repository; 
	
	private CacheManager cacheManager;
	
	private static Logger log = LoggerFactory.getLogger(EntityManagerImpl.class);
	
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
	
	@Override
	public Key allocateId(Class<?> clazz) {
		return datastoreService.allocateIds(clazz.getSimpleName(), 1).getStart();
	}
	
	private PagedQuery createPagedQueryImpl(Key ancestor, ClassMetadata metadata) {
		if (enforceSchemaConstraints && ancestor != null) { 
			metadata.validateParentKey(ancestor);
		}
		return new PagedQuery(this, ancestor, metadata);
	}
	
	private SimpleQuery createQueryImpl(Key ancestor, ClassMetadata metadata) {
		if (enforceSchemaConstraints && ancestor != null) { 
			metadata.validateParentKey(ancestor);
		}
		return new SimpleQuery(this, ancestor, metadata);
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
		PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
		Key providedKey = keyProperty.getValue(javaObject);
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
		
		// cache the stored value
		if (metadata.isCacheable()) {
			cacheManager.put(javaObject, entity, metadata);
		}
		return newKey;
	}
	
	@Override
	public <T> void put(Collection<T> javaObjects) {
		put(null, null, javaObjects);
	}
	
	@Override
	public <T> void put(Transaction transaction, Collection<T> javaObjects) {
		put(transaction, null, javaObjects);
	}
	
	@Override
	public <T> void put(Key parentKey, Collection<T> javaObjects) {
		put(null, parentKey, javaObjects);
	}
	
	@Override
	public <T> void put(Transaction transaction, Key parentKey, Collection<T> javaObjects) {
		
		Iterator<T> it = javaObjects.iterator();
		if (!it.hasNext()) {
			return;
		}
		
		// allocate and set missing primary keys (in bulk)
		List<T> transientInstances = Lists.newArrayListWithCapacity(javaObjects.size());
		ClassMetadata metadata = repository.get(javaObjects.iterator().next().getClass());
		PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
		for (T javaObject : javaObjects) {
			Key key = keyProperty.getValue(javaObject);
			if (key == null) {
				if (!metadata.isGenerateKeyValue()) {
					throw new IllegalArgumentException("No key value provided for " + javaObject + ", but key generation is not enabled for " + metadata.getKind() + " (missing @GeneratedValue?)");
				}
				transientInstances.add(javaObject);
			}
		}

		if (!transientInstances.isEmpty()) {
			if (enforceSchemaConstraints) {
				metadata.validateParentKey(parentKey);
			}
			Iterator<Key> allocatedKeys = datastoreService.allocateIds(parentKey, metadata.getKind(), transientInstances.size()).iterator();
			for (T javaObject : transientInstances) {
				keyProperty.setValue(javaObject, allocatedKeys.next());
			}
		}
		
		// transform to entity instances and persist
		List<T> cacheableInstances = Lists.newArrayListWithCapacity(javaObjects.size());
		List<Entity> cacheableEntities = Lists.newArrayListWithCapacity(javaObjects.size());
		List<Entity> entities = new ArrayList<Entity>(javaObjects.size());
		for (T javaObject : javaObjects) {
			Entity entity = metadata.javaToDatastore(parentKey, javaObject);
			
			// check required fields
			if (enforceSchemaConstraints) {
				metadata.validateConstraints(entity);
			}
			
			entities.add(entity);
			if (metadata.isCacheable()) {
				cacheableInstances.add(javaObject);
				cacheableEntities.add(entity);
			}
		}
		
		
		// persist 
		datastoreService.put(transaction, entities);
		
		// store in cache
		if (!cacheableInstances.isEmpty()) {
			cacheManager.put(cacheableInstances, cacheableEntities, metadata);
		}
	}
	
	@Override
	public void deleteQuietly(Key... keys) {
		deleteQuietly(Arrays.asList(keys));
	}
	
	@Override
	public void deleteQuietly(Iterable<Key> keys) {
		try {
			delete(null, keys);
		} catch (Exception e) {
			log.debug("Ignored: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void delete(Key... keys) {
		delete(null, keys);
	}
	
	@Override
	public void delete(Transaction transaction, Key... keys) {
		delete(transaction, Arrays.asList(keys));
	}
	
	@Override
	public void delete(Iterable<Key> keys) {
		delete(null, keys);
	}
	
	@Override
	public void delete(Transaction transaction, Iterable<Key> keys) {
		List<Key> cacheableKeys = Lists.newArrayListWithCapacity(keys instanceof Collection? ((Collection<Key>) keys).size() : 10);
		for (Key key : keys) {
			ClassMetadata metadata = repository.get(key.getKind());
			if (metadata.isCacheable()) {
				cacheableKeys.add(key);
			}
		}
		datastoreService.delete(transaction, keys);
		if (!cacheableKeys.isEmpty()) {
			cacheManager.delete(cacheableKeys);
		}
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
			ClassMetadata metadata = repository.get(key.getKind());
			T javaObject;
			if (metadata.isCacheable() && transaction == null) { // ignore cache if a transaction is active
				javaObject = (T) cacheManager.get(key, metadata);
				if (javaObject != null) {
					return javaObject;
				}
			}
			Entity entity = datastoreService.get(transaction, key);
			javaObject = (T) metadata.datastoreToJava(entity);
			if (metadata.isCacheable()) {
				cacheManager.put(javaObject, entity, metadata);
			}
			return javaObject;
		} catch (EntityNotFoundException e) {
			throw new org.simpleds.exception.EntityNotFoundException(e);
		}
	}
	
	@Override
	public void refresh(Object instance) {
		try {
			ClassMetadata metadata = repository.get(instance.getClass());
			PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
			Entity entity = datastoreService.get(keyProperty.getValue(instance));
			metadata.populate(entity, instance);
			if (metadata.isCacheable()) {
				cacheManager.put(instance, entity, metadata);
			}
		} catch (EntityNotFoundException e) {
			throw new org.simpleds.exception.EntityNotFoundException(e);
		}
	}
	
	@Override
	public <T> List<T> get(Iterable<Key> keys) {
		return get(null, keys);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> get(Transaction transaction, Iterable<Key> keys) {
		Iterator<Key> itKey = keys.iterator();
		if (!itKey.hasNext()) {
			return (List<T>) Lists.newArrayList();
		}
		
		ClassMetadata metadata = repository.get(itKey.next().getKind());
		List<T> result;
		if (metadata.isCacheable() && transaction == null) { 
			
			// retrieve cached and non-cached data
			Collection keysCollection = keys instanceof Collection? (Collection) keys : Lists.newArrayList(keys);
			Map<Key, T> cachedValues = cacheManager.get(keysCollection, metadata);
			Iterable<Key> noncachedKeys = Iterables.filter(keys, new NonCachedPredicate(cachedValues.keySet()));
			Map<Key, Entity> noncachedEntitiesMap = datastoreService.get(transaction, noncachedKeys);
			List<T> noncachedValues = Lists.newArrayListWithCapacity(noncachedEntitiesMap.size());
			List<Entity> noncachedEntities = Lists.newArrayListWithCapacity(noncachedEntitiesMap.size());
			
			// merge both
			result = Lists.newArrayListWithCapacity(keysCollection.size());
			for (Key key : keys) {
				T javaObject = cachedValues.get(key);
				if (javaObject == null) {
					Entity entity = noncachedEntitiesMap.get(key);
					if (entity != null) {
						javaObject = (T) metadata.datastoreToJava(entity);
						noncachedValues.add(javaObject);
						noncachedEntities.add(entity);
					}
				}
				result.add(javaObject);
			}
			if (!noncachedValues.isEmpty()) {
				cacheManager.put(noncachedValues, noncachedEntities, metadata);
			}
			
		} else { // not cacheable
			
			Map<Key, Entity> entities = datastoreService.get(transaction, keys);
			result = Lists.newArrayListWithCapacity(keys instanceof Collection? ((Collection) keys).size() : 10);
			for (Key key : keys) {
				Entity entity = entities.get(key);
				T javaObject = (T) metadata.datastoreToJava(entity);
				result.add(javaObject);
			}
			
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
	
	
	public void setDatastoreService(DatastoreService service) {
		this.datastoreService = service;
	}
	
	public void setRepository(PersistenceMetadataRepository repository) {
		this.repository = repository;
	}
	
	public void setEnforceSchemaConstraints(boolean enforceSchemaConstraints) {
		this.enforceSchemaConstraints = enforceSchemaConstraints;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}
		
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> find(SimpleQuery simpleQuery) {
		return simpleQuery.asList();
	}

	@Override
	public <T> T findSingle(SimpleQuery query) {
		return (T) query.asSingleResult();
	}
	
	@Override
	public <T> SimpleQueryResultIterable<T> asIterable(SimpleQuery query) {
		return query.asIterable();
	}
	
	@Override
	public <T> SimpleQueryResultIterator<T> asIterator(SimpleQuery simpleQuery) {
		return simpleQuery.asIterator();
	}
	
	@Override
	public <T> List<T> findChildren(Key parentKey, Class<T> childrenClass) {
		return find(createQuery(parentKey, childrenClass));
	}
	
	@Override
	public List<Key> findChildrenKeys(Key parentKey, Class<?> childrenClass) {
		return find(createQuery(parentKey, childrenClass).keysOnly());
	}
	
	@Override
	public int count(SimpleQuery query) {
		return query.count();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> PagedList<T> findPaged(PagedQuery query) {
		return query.asPagedList();
	}

	@Override
	public DatastoreService getDatastoreService() {
		return datastoreService;
	}
	
	
}
