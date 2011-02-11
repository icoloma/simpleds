package org.simpleds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.simpleds.cache.CacheManager;
import org.simpleds.cache.NonCachedPredicate;
import org.simpleds.functions.EntityToKeyFunction;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.metadata.PropertyMetadata;
import org.simpleds.metadata.VersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@Singleton
public class EntityManagerImpl implements EntityManager {

	@Inject
	private DatastoreService datastoreService;
	
	@Inject
	private PersistenceMetadataRepository persistenceMetadataRepository; 
	
	@Inject
	private CacheManager cacheManager;
	
	private static Logger log = LoggerFactory.getLogger(EntityManagerImpl.class);
	
	/** true to check the schema constraints before persisting changes to the database, default true */
	private boolean enforceSchemaConstraints = true;
	
	public EntityManagerImpl() {
		EntityManagerFactory.setEntityManager(this);
	}
	
	@Override
	public Transaction beginTransaction() {
		return datastoreService.beginTransaction();
	}
	
	@Override
	public ClassMetadata getClassMetadata(Class<?> clazz) {
		return persistenceMetadataRepository.get(clazz);
	}
	
	@Override
	public ClassMetadata getClassMetadata(String kind) {
		return persistenceMetadataRepository.get(kind);
	}
	
	@Override
	public SimpleQuery createQuery(String kind) {
		return createQueryImpl(null, persistenceMetadataRepository.get(kind));
	}
	
	@Override
	public SimpleQuery createQuery(Class<?> clazz) {
		return createQueryImpl(null, persistenceMetadataRepository.get(clazz));
	}
	
	@Override
	public SimpleQuery createQuery(Key ancestor, String kind) {
		return createQueryImpl(ancestor, persistenceMetadataRepository.get(kind));
	}
	
	@Override
	public SimpleQuery createQuery(Key ancestor, Class<?> clazz) {
		return createQueryImpl(ancestor, persistenceMetadataRepository.get(clazz));
	}
	
	@Override
	public PagedQuery createPagedQuery(String kind) {
		return createPagedQueryImpl(null, persistenceMetadataRepository.get(kind));
	}
	
	@Override
	public PagedQuery createPagedQuery(Class<?> clazz) {
		return createPagedQueryImpl(null, persistenceMetadataRepository.get(clazz));
	}
	
	@Override
	public PagedQuery createPagedQuery(Key ancestor, String kind) {
		return createPagedQueryImpl(ancestor, persistenceMetadataRepository.get(kind));
	}
	
	@Override
	public PagedQuery createPagedQuery(Key ancestor, Class<?> clazz) {
		return createPagedQueryImpl(ancestor, persistenceMetadataRepository.get(clazz));
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
		ClassMetadata metadata = persistenceMetadataRepository.get(javaObject.getClass());
		VersionManager versionManager = metadata.getVersionManager();
		PropertyMetadata versionProperty = versionManager == null? null : versionManager.getPropertyMetadata();
		Object newVersionValue = null;
		
		// check if the key is missing
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
		
		// retrieve and check the @Version attribute, if any
		if (versionManager != null) {
			if (providedKey != null) {
				try {
					if (transaction == null) {
						log.warn("Storing versioned instance " + providedKey + " without a transaction. Be aware that optimistic locking may not be accurate unless you provide with a transaction");
					}
					Entity currentEntity = datastoreService.get(transaction, providedKey);
					newVersionValue = versionManager.validateVersion(currentEntity, javaObject);
				} catch (EntityNotFoundException e) {
					// safely ignore this, the entity does not yet exist
				}
			}
			if (newVersionValue == null) {
				newVersionValue = versionManager.getStartValue();
			}
			versionProperty.setEntityValue(entity, newVersionValue);
		}
		
		// persist 
		Key newKey = datastoreService.put(transaction, entity);
		
		// set the returned primary key value
		if (providedKey == null) {
			keyProperty.setValue(javaObject, newKey);
		}
		
		// set the new version value
		if (versionManager != null) {
			versionProperty.setValue(javaObject, newVersionValue);
		}
		
		// cache the stored value
		if (metadata.isCacheable()) {
			cacheManager.put(javaObject, entity, metadata);
		}
		return newKey;
	}
	
	@Override
	public void put(Collection<?> javaObjects) {
		put(null, null, javaObjects);
	}
	
	@Override
	public void put(Transaction transaction, Collection<?> javaObjects) {
		put(transaction, null, javaObjects);
	}
	
	@Override
	public void put(Key parentKey, Collection<?> javaObjects) {
		put(null, parentKey, javaObjects);
	}
	
	@Override
	public void put(Transaction transaction, Key parentKey, Collection<?> javaObjects) {
		
		Iterator<?> it = javaObjects.iterator();
		if (!it.hasNext()) {
			return;
		}
		
		// separate instances by ClassMetadata
		ListMultimap<ClassMetadata, Object> transientInstances = ArrayListMultimap.create(16, javaObjects.size());
		ListMultimap<ClassMetadata, Object> versionedInstances = ArrayListMultimap.create();
		for (Object javaObject : javaObjects) {
			Class<? extends Object> clazz = javaObject.getClass();
			ClassMetadata metadata = persistenceMetadataRepository.get(clazz);
			PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
			Key key = keyProperty.getValue(javaObject);
			if (key == null) {
				if (!metadata.isGenerateKeyValue()) {
					throw new IllegalArgumentException("No key value provided for " + javaObject + ", but key generation is not enabled for " + metadata.getKind() + " (missing @GeneratedValue?)");
				}
				transientInstances.put(metadata, javaObject);
			}
			VersionManager versionManager = metadata.getVersionManager();
			if (versionManager != null) {
				versionedInstances.put(metadata, javaObject);
			}
		}

		// retrieve current @Version values
		Map<Key, Object> newVersionValues = Maps.newHashMap();
		for (ClassMetadata metadata : versionedInstances.keySet()) {
			VersionManager versionManager = metadata.getVersionManager();
			Collection<Object> javaObjects = versionedInstances.get(metadata);
			for (Object javaObject : javaObjects) {
				PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
				Key key = keyProperty.getValue(javaObject);
				if (key != null) {
					try {
						Map<Key, Entity> currentEntities = datastoreService.get(transaction, Collections2.transform(javaObjects, new EntityToKeyFunction(metadata.getClass())));
						for (Map.Entry<Key, Entity> versionedEntityEntry : currentEntities.entrySet()) {
							Object newVersionValue = versionManager.validateVersion(versionedEntityEntry.getValue(), javaObject);
							newVersionValues.put(versionedEntityEntry.getKey(), newVersionValue);
						}
					} catch (EntityNotFoundException e) {
						// safely ignore this, the entity does not yet exist
					}
				}
				if (newVersionValue == null) {
					newVersionValue = versionManager.getStartValue();
				}
				versionProperty.setEntityValue(entity, newVersionValue);
			}
			if (transaction == null && !newVersionValues.isEmpty()) {
				log.warn("Storing ", newVersionValues.size(), " instances of versioned ", metadata.getKind(), " without a transaction. Be aware that optimistic locking may not be accurate unless you provide with a transaction");
			}
		}
		
		// assign generated keys
		for (ClassMetadata metadata : transientInstances.keySet()) {
			PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
			if (enforceSchemaConstraints) {
				metadata.validateParentKey(parentKey);
			}
			List<Object> instances = transientInstances.get(metadata);
			Iterator<Key> allocatedKeys = datastoreService.allocateIds( parentKey, metadata.getKind(), instances.size()).iterator();
			for (Object javaObject : instances) {
				keyProperty.setValue(javaObject, allocatedKeys.next());
			}
		}
		
		// transform to entity instances 
		ListMultimap<ClassMetadata, Object> cacheableInstances = ArrayListMultimap.create(16, javaObjects.size());
		ListMultimap<ClassMetadata, Entity> cacheableEntities = ArrayListMultimap.create(16, javaObjects.size());
		List<Entity> entities = Lists.newArrayListWithCapacity(javaObjects.size());
		for (Object javaObject : javaObjects) {
			Class<? extends Object> clazz = javaObject.getClass();
			ClassMetadata metadata = persistenceMetadataRepository.get(clazz);
			Entity entity = metadata.javaToDatastore(parentKey, javaObject);
			
			// check required fields
			if (enforceSchemaConstraints) {
				metadata.validateConstraints(entity);
			}
			
			entities.add(entity);
			if (metadata.isCacheable()) {
				cacheableInstances.put(metadata, javaObject);
				cacheableEntities.put(metadata, entity);
			}
		}
		
		
		// persist 
		datastoreService.put(transaction, entities);
		
		// store in cache
		cacheManager.put(cacheableInstances, cacheableEntities);
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
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
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
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
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
			ClassMetadata metadata = persistenceMetadataRepository.get(instance.getClass());
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
	public <T> Map<Key, T> get(Iterable<Key> keys) {
		return get(null, keys);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<Key, T> get(Transaction transaction, Iterable<Key> unsortedKeys) {
		Multimap<ClassMetadata, Key> sortedKeys = ArrayListMultimap.create();
		for (Key key : unsortedKeys) {
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
			sortedKeys.put(metadata, key);
		}
		
		// retrieve values from cache only if tx != null
		Map<Key, Object> cachedValues = transaction != null? (Map)ImmutableMap.of() : cacheManager.get(sortedKeys);
		
		// retrieve values from database
		Iterable<Key> cacheMissKeys = Iterables.filter(unsortedKeys, new NonCachedPredicate(cachedValues.keySet()));
		Map<Key, Entity> cacheMissEntities = datastoreService.get(transaction, cacheMissKeys);
		
		// transform into java objects
		ListMultimap<ClassMetadata, Object> populateCacheValues = ArrayListMultimap.create();
		ListMultimap<ClassMetadata, Entity> populateCacheEntities = ArrayListMultimap.create();
		
		Map<Key, Object> result = Maps.newHashMapWithExpectedSize(cachedValues.size() + cacheMissEntities.size());
		result.putAll(cachedValues);
		for (Entity entity : cacheMissEntities.values()) {
			if (entity != null) {
				Key key = entity.getKey();
				ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
				Object javaObject = metadata.datastoreToJava(entity);
				result.put(key, javaObject);
				if (metadata.isCacheable()) {
					populateCacheValues.put(metadata, javaObject);
					populateCacheEntities.put(metadata, entity);
				}
			}
		}
		
		if (!populateCacheValues.isEmpty()) {
			cacheManager.put(populateCacheValues, populateCacheEntities);
		}
				
		return (Map) result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T datastoreToJava(Entity entity) {
		ClassMetadata metadata = persistenceMetadataRepository.get(entity.getKind());
		return (T)metadata.datastoreToJava(entity);
	}
	
	@Override
	public Entity javaToDatastore(Object javaObject) {
		ClassMetadata metadata = persistenceMetadataRepository.get(javaObject.getClass());
		return metadata.javaToDatastore(null, javaObject);
	}
	
	
	public void setDatastoreService(DatastoreService service) {
		this.datastoreService = service;
	}
	
	public void setPersistenceMetadataRepository(PersistenceMetadataRepository repository) {
		this.persistenceMetadataRepository = repository;
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
