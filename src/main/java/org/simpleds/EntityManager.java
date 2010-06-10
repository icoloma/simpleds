package org.simpleds;

import java.util.Collection;
import java.util.List;

import org.simpleds.cache.CacheManager;
import org.simpleds.exception.EntityNotFoundException;
import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

public interface EntityManager {

	/**
	 * Convert a Datastore representation to a Java object
	 * @param entity the Entity returned by the Datastore
	 * @return the Java equivalent to the provided {@link Entity}
	 */
	<T> T datastoreToJava(Entity entity);

	/**
	 * Convert a Java Object to its Datastore representation
	 * @param javaObject the Java Object to transform
	 * @return the {@link Entity} equivalent to the provided Java Object
	 */
	Entity javaToDatastore(Object javaObject);

	/**
	 * Persists a java object to the datastore. If the entity is cacheable, it will be written to the cache.
	 * If the primary key has not yet been assigned, a new one will be generated and assigned to the Java object.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Object javaObject);
	
	/**
	 * Persists a java object to the datastore. If the entity is cacheable, it will be written to the cache.
	 * If the primary key has not yet been assigned, a new one will be generated and assigned to the Java object.
	 * @param transaction the transaction instance to use.  May be null.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Transaction transaction, Object javaObject);

	/**
	 * Persists a java object to the datastore. If the entity is cacheable, it will be written to the cache.
	 * If the primary key has not yet been assigned, a new one will be generated and assigned to the Java object.
	 * @param parentKey the parent entity key to use while generating the persistent entity key. May be null.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Key parentKey, Object javaObject);

	/**
	 * Persists a java object to the datastore. If the entity is cacheable, it will be written to the cache.
	 * If the primary key has not yet been assigned, a new one will be generated and assigned to the Java object.
	 * @param parentKey the parent entity key to use while generating the persistent entity key. May be null.
	 * @param transaction the transaction instance to use.  May be null.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Transaction transaction, Key parentKey, Object javaObject);
	
	/**
	 * Store a collection of persistent objects into the datastore. 
	 * Primary keys will be generated and assigned if needed. If these objects are cacheable,
	 * the cache contents will also be updated.
	 * 
	 * @param javaObjects the Collection of java objects to store
	 */
	<T> void put(Collection<T> javaObjects);
	
	/**
	 * Store a collection of persistent objects into the datastore.
	 * Primary keys will be generated and assigned if needed. If these objects are cacheable,
	 * the cache contents will also be updated.
	 * @param javaObjects the Collection of java objects to store
	 * @param transaction the transaction instance to use.  May be null.
	 */
	<T> void put(Transaction transaction, Collection<T> javaObjects);

	/**
	 * Store a collection of persistent objects into the datastore.
	 * Primary keys will be generated and assigned if needed. If these objects are cacheable,
	 * the cache contents will also be updated.
	 * @param parentKey the key of the parent instance. If not null, it will be used as parent
	 * of the generated entity Keys
	 * @param javaObjects the Collection of java objects to store
	 */
	<T> void put(Key parentKey, Collection<T> javaObjects);
	
	/**
	 * Store a collection of persistent objects into the datastore.
	 * Primary keys will be generated and assigned if needed. If these objects are cacheable,
	 * the cache contents will also be updated.
	 * @param parentKey the key of the parent instance. If not null, it will be used as parent
	 * of the generated entity Keys
	 * @param transaction the transaction instance to use.  May be null.
	 * @param javaObjects the Collection of java objects to store
	 */
	<T> void put(Transaction transaction, Key parentKey, Collection<T> javaObjects);

	/**
	 * Return a persistent java instance by key
	 * @param key the key of the persistent entity to retrieve
	 * @return the persistent java instance
	 */
	<T> T get(Key key);
	
	/**
	 * Return a persistent java instance by key
	 * @param key the key of the persistent entity to retrieve
	 * @param transaction the transaction instance to use.  May be null.
	 * @return the persistent java instance
	 */
	<T> T get(Transaction transaction, Key key);

	/**
	 * Return a set of persistent entities, by key
	 * @param keys the keys of the persistent entities to retrieve
	 * @return a Map of the retrieved entities, by Key
	 */
	<T> List<T> get(Iterable<Key> keys);
	
	/**
	 * Return a set of persistent entities, by key
	 * @param keys the keys of the persistent entities to retrieve
	 * @param transaction the transaction instance to use.  May be null.
	 * @return a Map of the retrieved entities, by Key
	 */
	<T> List<T> get(Transaction transaction, Iterable<Key> keys);
	
	/**
	 * Delete multiple instances from the Datastore. Cached keys will also be removed from the datastore.
	 * @param keys the keys to delete. Notice that they can reference different entity kinds.
	 */
	void delete(Key... keys);
	
	/**
	 * Delete multiple instances from the Datastore. Cached keys will also be removed from the datastore.
	 * @param keys the keys to delete. Notice that they can reference different entity kinds.
	 * @param transaction the transaction instance to use.  May be null.
	 */
	void delete(Transaction transaction, Key... keys);
	
	/**
	 * Delete multiple instances from the Datastore. Cached keys will also be removed from the datastore.
	 * @param keys the keys to delete. Notice that they can reference different entity kinds.
	 */
	void delete(Iterable<Key> keys);
	
	/**
	 * Delete multiple instances from the Datastore. Cached keys will also be removed from the datastore.
	 * @param transaction the transaction instance to use.  May be null.
	 * @param keys the keys to delete. Notice that they can reference different entity kinds.
	 */
	void delete(Transaction transaction, Iterable<Key> keys);


	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param kind the unqualified class name for this query
	 */
	SimpleQuery createQuery(String kind);
	
	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param clazz the class for this query
	 */
	SimpleQuery createQuery(Class<?> clazz);
	
	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param kind the unqualified class name for this query
	 */
	SimpleQuery createQuery(Key ancestor, String kind);
	
	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param clazz the class for this query
	 */
	SimpleQuery createQuery(Key ancestor, Class<?> clazz);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param kind the unqualified class name for this query
	 */
	PagedQuery createPagedQuery(String kind);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param clazz the class for this query
	 */
	PagedQuery createPagedQuery(Class<?> clazz);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param kind the unqualified class name for this query
	 */
	PagedQuery createPagedQuery(Key ancestor, String kind);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param clazz the class for this query
	 */
	PagedQuery createPagedQuery(Key ancestor, Class<?> clazz);

	/**
	 * @return the configured {@link ClassMetadata} for the provided persistent class
	 * @throws IllegalArgumentException if the provided class is not registered as a persistent class
	 */
	ClassMetadata getClassMetadata(Class<?> clazz);

	/**
	 * @return the configured {@link ClassMetadata} for the provided kind
	 * @throws IllegalArgumentException if the provided kind is not registered as a persistent class
	 */
	ClassMetadata getClassMetadata(String kind);
	
	/**
	 * Wrapper method for DatastoreService.beginTransaction
	 * @return the created {@link Transaction}
	 */
	Transaction beginTransaction();

	/**
	 * @return the {@link CacheManager} instance used by this {@link EntityManager}
	 */
	CacheManager getCacheManager();

	/** 
	 * Execute the provided query and returns the result as a List of java objects 
	 * @param query the query to execute
	 * @return the list of resulting java entities
	 * @deprecated use SimpleQuery.find() instead
	 */
	@Deprecated
	<T> List<T> find(SimpleQuery query);

	/**
	 * Execute a query and return a single result
	 * @return the first result of the query
	 * @throws EntityNotFoundException if the query did not return any result
	 * @deprecated use SimpleQuery.findSingle() instead
	 */
	@Deprecated
	<T> T findSingle(SimpleQuery q);

	/**
	 * Counts the number of instances returned from the specified query. This method will only
	 * retrieve the matching keys, not the entities themselves.
	 * @deprecated use SimpleQuery.count() instead
	 */
	@Deprecated
	int count(SimpleQuery q);

	/**
	 * Return the list of children that have a provided parent instance
	 * @param parentKey the key of the parent instance
	 * @param childrenClass the class of the children to return
	 * @deprecated 	use entityManager.createQuery(parentKey, childrenClass).find() instead.
	 */
	@Deprecated
	<T> List<T> findChildren(Key parentKey, Class<T> childrenClass);

	/**
	 * Return the list of children keys that have a provided parent instance
	 * @param parentKey the key of the parent instance
	 * @param childrenClass the class of the children to return
	 * @return the list of keys of the children
	 * @deprecated 	use entityManager.createQuery(parentKey, childrenClass).keysOnly().find() instead.
	 */
	@Deprecated
	List<Key> findChildrenKeys(Key parentKey, Class<?> childrenClass);

	/**
	 * Return a {@link PagedList} result after computing a PagedQuery
	 * @param query the query to execute
	 * @return the result of the query
	 * @deprecated use PagedQuery.find() instead
	 */
	@Deprecated
	<T> PagedList<T> findPaged(PagedQuery query);
	
	/** 
	 * Execute the provided query and returns the result as a {@link SimpleQueryResultIterable} of java objects.
	 * This method does not check the cache.
	 * @param query the query to execute
	 * @return the list of resulting java entities
	 * @deprecated use SimpleQuery.asIterable() instead
	 */
	@Deprecated
	<T> SimpleQueryResultIterable<T> asIterable(SimpleQuery query);
	
	/** 
	 * Execute the provided query and returns the result as a {@link SimpleQueryResultIterator} of java objects
	 * This method does not check the cache.
	 * @param query the query to execute
	 * @return the list of resulting java entities
	 * @deprecated use SimpleQuery.asIterator() instead
	 */
	@Deprecated
	<T> SimpleQueryResultIterator<T> asIterator(SimpleQuery query);

	/**
	 * @return the {@link DatastoreService} used by this instance.
	 */
	DatastoreService getDatastoreService();

}
