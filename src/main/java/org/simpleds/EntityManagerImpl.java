package org.simpleds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.*;
import org.simpleds.cache.CacheManager;
import org.simpleds.cache.NonCachedPredicate;
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
import com.google.appengine.api.datastore.TransactionOptions;

@Singleton
@SuppressWarnings({ "unchecked", "rawtypes" })
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
	public Transaction beginTransaction(TransactionOptions options) {
		return datastoreService.beginTransaction(options);
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
	public Key allocateId(Class<?> clazz) {
		String kind = persistenceMetadataRepository.get(clazz).getKind();
		return datastoreService.allocateIds(kind, 1).getStart();
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
		
		// instances without provided primary key
		ListMultimap<ClassMetadata, Object> transientInstances = ArrayListMultimap.create();
		
		// versioned instances
		ListMultimap<ClassMetadata, Object> versionedInstances = ArrayListMultimap.create();
		
		// provided (not null) keys for versioned instances
		Set<Key> versionedProvidedKeys = Sets.newHashSet(); 
		
		// separate instances
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
				if (key != null) {
					versionedProvidedKeys.add(key);
				}
			}
		}

		// retrieve current @Version values for existing entities
		Map<Key, Entity> currentVersionedEntities = versionedInstances.isEmpty()? null : datastoreService.get(transaction, versionedProvidedKeys);
		Map<Key, Object> newVersionValues = Maps.newHashMap();
		for (ClassMetadata metadata : versionedInstances.keySet()) {
			VersionManager versionManager = metadata.getVersionManager();
			PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
			List<Object> vjo = versionedInstances.get(metadata);
			for (Object javaObject : vjo) {
				Key key = keyProperty.getValue(javaObject);
				if (key != null) {
					Entity currentEntity = currentVersionedEntities.get(key);
					Object newVersionValue = currentEntity == null? 
							versionManager.getStartValue() : // it does not exist
							versionManager.validateVersion(currentEntity, javaObject); // it does exist, retrieve next value
					newVersionValues.put(key, newVersionValue);
				}
			}
			if (transaction == null && !vjo.isEmpty()) {
				log.warn("Storing " + vjo.size() + " instances of versioned " + metadata.getKind() + " without a transaction. Be aware that optimistic locking may not be accurate unless you provide with a transaction");
			}
		}
		
		// assign generated keys and start version values to transient entities
		for (ClassMetadata metadata : transientInstances.keySet()) {
			PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
			VersionManager versionManager = metadata.getVersionManager();
			Object startVersion = versionManager == null? null : versionManager.getStartValue();
			if (enforceSchemaConstraints) {
				metadata.validateParentKey(parentKey);
			}
			List<Object> instances = transientInstances.get(metadata);
			Iterator<Key> allocatedKeys = datastoreService.allocateIds( parentKey, metadata.getKind(), instances.size()).iterator();
			for (Object javaObject : instances) {
				Key key = allocatedKeys.next();
				keyProperty.setValue(javaObject, key);
				if (versionManager != null) {
					newVersionValues.put(key, startVersion);
				}
			}
		}
		
		// transform to entity instances 
		ListMultimap<ClassMetadata, Object> cacheableInstances = ArrayListMultimap.create(16, javaObjects.size());
		ListMultimap<ClassMetadata, Entity> cacheableEntities = ArrayListMultimap.create(16, javaObjects.size());
		List<Entity> entities = Lists.newArrayListWithCapacity(javaObjects.size());
		for (Object javaObject : javaObjects) {
			Class<? extends Object> clazz = javaObject.getClass();
			ClassMetadata metadata = persistenceMetadataRepository.get(clazz);
			VersionManager versionManager = metadata.getVersionManager();
			Entity entity = metadata.javaToDatastore(parentKey, javaObject);
			
			// inject version value
			if (versionManager != null) {
				Object newVersionValue = newVersionValues.get(entity.getKey());
				versionManager.getPropertyMetadata().setEntityValue(entity, newVersionValue);
			}
			
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
		
		// set new version values into java objects
		for (ClassMetadata metadata : versionedInstances.keySet()) {
			VersionManager versionManager = metadata.getVersionManager();
			PropertyMetadata<Key, Key> keyProperty = metadata.getKeyProperty();
			for (Object javaObject : versionedInstances.get(metadata)) {
				Key key = keyProperty.getValue(javaObject);
				Object newVersionValue = newVersionValues.get(key);
				versionManager.getPropertyMetadata().setValue(javaObject, newVersionValue);
			}
		}
		
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
        SetMultimap<String, Key> cacheableKeys = HashMultimap.create();
		for (Key key : keys) {
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
			if (metadata.isCacheable()) {
				cacheableKeys.put(metadata.getCacheNamespace(), key);
			}
		}
		datastoreService.delete(transaction, keys);
		if (!cacheableKeys.isEmpty()) {
			cacheManager.delete(cacheableKeys);
		}
	}
	
	@Override
	public <T> T get(Key key) {
		return (T) get(null, key);
	}
	
	@Override
	public <T> T get(Transaction transaction, Key key) {
		try {
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
			T javaObject;
			if (metadata.isCacheable() && transaction == null) { // ignore cache if a transaction is active
				javaObject = (T) cacheManager.get(metadata, key);
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
	
	@Override
	public <T> Map<Key, T> get(Transaction transaction, Iterable<Key> uk) {
        Set<Key> unsortedKeys = uk instanceof Set? (Set<Key>) uk : Sets.newHashSet(uk);
		SetMultimap<ClassMetadata, Key> cacheKeys = HashMultimap.create();
		for (Key key : unsortedKeys) {
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
			cacheKeys.put(metadata, key);
		}
		
		// retrieve values from cache only if tx != null
		Map<Key, Object> cachedValues = transaction != null? (Map)ImmutableMap.of() : cacheManager.get(cacheKeys);
		
		// retrieve values from database
		Map<Key, Entity> cacheMissEntities = datastoreService.get(transaction, Sets.difference(unsortedKeys, cachedValues.keySet()));
		
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
	public <T> List<T> find(SimpleQuery simpleQuery) {
		return simpleQuery.asList();
	}

	@Override
	public <T> T findSingle(SimpleQuery query) {
		return (T) query.asSingleResult();
	}
	
	@Override
	public <T> CursorIterable<T> asIterable(SimpleQuery query) {
		return query.asIterable();
	}
	
	@Override
	public <T> CursorIterator<T> asIterator(SimpleQuery simpleQuery) {
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
	public DatastoreService getDatastoreService() {
		return datastoreService;
	}
	
}
