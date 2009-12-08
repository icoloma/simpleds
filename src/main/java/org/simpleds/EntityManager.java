package org.simpleds;

import java.util.Collection;
import java.util.List;

import org.simpleds.exception.EntityNotFoundException;
import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

public interface EntityManager {

	/** 
	 * Execute the provided query and returns the result as a List of java objects 
	 * @param query the query to execute
	 * @return the list of resulting java entities
	 */
	<T> List<T> find(SimpleQuery query);

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
	 * Persists a java object to the datastore. This method is equivalent to invoking merge(null, javaObject)
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Object javaObject);
	
	/**
	 * Persists a java object to the datastore. This method is equivalent to invoking merge(null, javaObject)
	 * @param transaction the transaction instance to use.  May be null.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Transaction transaction, Object javaObject);

	/**
	 * Persists a java object to the datastore. 
	 * If the primary key has not yet been assigned, a new one will be generated and assigned.
	 * The entity will also be checked for missing required fields 
	 * @param parentKey the parent entity key that will be used to generate the persisted entity key. May be null.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Key parentKey, Object javaObject);

	/**
	 * Persists a java object to the datastore. 
	 * If the primary key has not yet been assigned, a new one will be generated and assigned.
	 * The entity will also be checked for missing required fields 
	 * @param parentKey the parent entity key that will be used to generate the persisted entity key. May be null.
	 * @param transaction the transaction instance to use.  May be null.
	 * @param javaObject the instance to persist.
	 * @return the allocated/existing key
	 */
	Key put(Transaction transaction, Key parentKey, Object javaObject);
	
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
	 * Creates a new managed {@link Transaction}. Transactions created using this 
	 * method will be registered with the current thread until either commit() or rollback() 
	 * is invoked.
	 */
	Transaction beginTransaction();
	
	/**
	 * Commits all transactions created using beginTransaction. Only active 
	 * transactions will be processed, any transaction that has been 
	 * manually commited or rolled back will be skipped.
	 * @throws DatastoreFailureException - If a datastore error occurs.
	 */
	public void commit();
	
	/**
	 * Rollbacks all transactions created using beginTransaction. Only active 
	 * transactions will be processed, any transaction that has been 
	 * manually commited or rolled back will be skipped.
	 * @throws DatastoreFailureException - If a datastore error occurs.
	 */
	public void rollback();

	/**
	 * Wrapper method around DatastoreService.delete()
	 */
	void delete(Key... keys);
	
	/**
	 * Wrapper method around DatastoreService.delete()
	 * @param transaction the transaction instance to use.  May be null.
	 */
	void delete(Transaction transaction, Key... keys);
	
	/**
	 * Wrapper method around DatastoreService.delete()
	 */
	void delete(Iterable<Key> keys);
	
	/**
	 * Wrapper method around DatastoreService.delete()
	 * @param transaction the transaction instance to use.  May be null.
	 */
	void delete(Transaction transaction, Iterable<Key> keys);

	/**
	 * Execute a query and return a single result
	 * @return the first result of the query
	 * @throws EntityNotFoundException if the query did not return any result
	 */
	<T> T findSingle(SimpleQuery q);

	/**
	 * Counts the number of instances returned from the specified query. This method will only
	 * retrieve the matching keys, not the entities themselves.
	 */
	int count(SimpleQuery q);

	/**
	 * Return the list of children that have a provided parent instance
	 * @param parentKey the key of the parent instance
	 * @param childrenClass the class of the children to return
	 */
	<T> List<T> findChildren(Key parentKey, Class<T> childrenClass);

	/**
	 * Return the list of children keys that have a provided parent instance
	 * @param parentKey the key of the parent instance
	 * @param childrenClass the class of the children to return
	 * @return the list of keys of the children
	 */
	List<Key> findChildrenKeys(Key parentKey, Class childrenClass);

	/**
	 * Store a set of persistent objects in the datastore
	 * @param javaObjects the list of java objects to store
	 */
	void put(Collection javaObjects);
	
	/**
	 * Store a set of persistent objects in the datastore
	 * @param javaObjects the list of java objects to store
	 * @param transaction the transaction instance to use.  May be null.
	 */
	void put(Transaction transaction, Collection javaObjects);

	/**
	 * Store a set of persistent objects in the datastore. 
	 * @param parentKey the key of the parent instance. If not null, all the provided objects 
	 * will get a primary key automatically generated
	 * @param javaObjects the list of java objects to store
	 */
	void put(Key parentKey, Collection javaObjects);
	
	/**
	 * Store a set of persistent objects in the datastore. 
	 * @param parentKey the key of the parent instance. If not null, all the provided objects 
	 * will get a primary key automatically generated
	 * @param transaction the transaction instance to use.  May be null.
	 * @param javaObjects the list of java objects to store
	 */
	void put(Transaction transaction, Key parentKey, Collection javaObjects);

	/**
	 * Return a {@link PagedList} result after computing a PagedQuery
	 * @param query the query to execute
	 * @return the result of the query
	 */
	<T> PagedList<T> findPaged(PagedQuery query);

	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param kind the unqualified class name for this query
	 */
	public SimpleQuery createQuery(String kind);
	
	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param clazz the class for this query
	 */
	public SimpleQuery createQuery(Class<?> clazz);
	
	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param kind the unqualified class name for this query
	 */
	public SimpleQuery createQuery(Key ancestor, String kind);
	
	/**
	 * Create a new {@link SimpleQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param clazz the class for this query
	 */
	public SimpleQuery createQuery(Key ancestor, Class<?> clazz);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param kind the unqualified class name for this query
	 */
	public PagedQuery createPagedQuery(String kind);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param clazz the class for this query
	 */
	public PagedQuery createPagedQuery(Class<?> clazz);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param kind the unqualified class name for this query
	 */
	public PagedQuery createPagedQuery(Key ancestor, String kind);
	
	/**
	 * Create a new {@link PagedQuery} instance
	 * @param ancestor the parent key to use for this query. Can be null.
	 * @param clazz the class for this query
	 */
	public PagedQuery createPagedQuery(Key ancestor, Class<?> clazz);

	/**
	 * @return the configured {@link ClassMetadata} for the provided persistent class
	 * @throws IllegalArgumentException if the provided class is not registered as a persistent class
	 */
	ClassMetadata getClassMetadata(Class<?> clazz);

	

}
