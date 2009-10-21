package org.simpleds;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.simpleds.exception.EntityNotFoundException;

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
	 * @param entity the entity to persist.
	 * @return the allocated/existing key
	 */
	Key put(Object javaObject);

	/**
	 * Persists a java object to the datastore. 
	 * If the primary key has not yet been assigned, a new one will be generated and assigned.
	 * The entity will also be checked for missing required fields 
	 * @param parentKey
	 * @param javaObject
	 * @return the allocated/existing key
	 */
	Key put(Key parentKey, Object javaObject);

	/**
	 * Return a persistent entity, by key
	 * @param key the key of the persistent entity to retrieve
	 * @return the persistent entity
	 */
	<T> T get(Key key);

	/**
	 * Return a set of persistent entities, by key
	 * @param keys the keys of the persistent entities to retrieve
	 * @return a Map of the retrieved entities, by Key
	 */
	Map<Key, Object> get(Iterable<Key> keys);
	
	/**
	 * Wrapper method around DatastoreService.beginTransaction()
	 */
	Transaction beginTransaction();
	
	/**
	 * Wrapper method around DatastoreService.getCurrentTransaction()
	 */
	Transaction getCurrentTransaction();

	/**
	 * Wrapper method around DatastoreService.delete()
	 */
	void delete(Key... keys);
	void delete(Iterable<Key> keys);

	/**
	 * Execute a query and return a single result
	 * @return the first result of the query
	 * @throws EntityNotFoundException if the query did not return any result
	 */
	<T> T findSingle(SimpleQuery q);

	/**
	 * Wrapper method around PreparedStatement.count()
	 */
	int count(SimpleQuery q);

	/**
	 * Get the value of a relation index
	 * @param entityKey the key of the persistent entity
	 * @param indexName the name of the relation index
	 */
	<T extends Collection> T getRelationIndex(Key entityKey, String indexName);

	/**
	 * Store the value of a relation index
	 * @param entityKey the key of the persistent entity
	 * @param indexName the name of the relation index
	 * @param indexValue the value of the relation index
	 */
	void setRelationIndex(Key entityKey, String indexName, Collection<?> indexValue);

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
	 * @param parentKey the key of the parent objects. If not null, all the provided objects 
	 * will a primary key automatically generated, providing they all have the same type
	 * @param javaObjects the list of java objects to store
	 */
	void put(Key parentKey, Collection javaObjects);

	/**
	 * Return a PagedList result after computing a PagedQuery
	 * @param query the query to execute
	 * @return the result of the query
	 */
	<T> PagedList<T> findPaged(PagedQuery query);

}
