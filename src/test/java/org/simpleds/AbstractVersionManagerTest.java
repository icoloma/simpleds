package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.exception.OptimisticLockException;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;
import org.simpleds.metadata.VersionManager;

import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ImmutableList;

public abstract class AbstractVersionManagerTest<T> extends AbstractEntityManagerTest {

	protected abstract T createEntity();
	
	protected VersionManager versionManager;
	
	protected PropertyMetadata versionProperty;

	/** null or 0L, depending on test subclass */
	protected T defaultValue;
	
	@Before
	public void setupVersionManager() {
		Class clazz = createEntity().getClass();
		ClassMetadata cm = repository.add(clazz);
		versionManager = cm.getVersionManager();
		versionProperty = versionManager.getPropertyMetadata();
	}
	
	private List<Object> createEntities() {
		return (List<Object>)ImmutableList.of(createEntity(), createEntity(), createEntity());
	}
	
	@Test
	public void testSinglePutSuccess() {
		T entity = createEntity();
		assertEquals(defaultValue, versionProperty.getValue(entity));
		entityManager.put(entity);
		
		// first value
		Object startValue = versionProperty.getValue(entity);
		assertNotNull(startValue);
		
		// second value
		entityManager.put(entity);
		assertFalse(startValue.equals(versionProperty.getValue(entity)));
	}
	
	@Test(expected=OptimisticLockException.class)
	public void testSinglePutOptimisticLockNull() {
		T entity = createEntity();
		entityManager.put(entity);
		entityManager.put(entity); // second update because of the primitive test
		
		versionProperty.setValue(entity, defaultValue);
		entityManager.put(entity);
	}
	
	@Test(expected=OptimisticLockException.class)
	public void testSinglePutOptimisticLockNotNull() {
		T entity = createEntity();
		entityManager.put(entity);
		Object startValue = versionProperty.getValue(entity);
		
		entityManager.put(entity);
		versionProperty.setValue(entity, startValue);
		entityManager.put(entity);
	}
	
	@Test
	public void testSinglePutTransaction() {
		T entity = createEntity();
		entityManager.put(entity);
		Object versionValue = versionProperty.getValue(entity);
		
		Transaction tx = entityManager.beginTransaction();
		entityManager.put(tx, entity);
		tx.rollback();
		assertFalse(versionValue.equals(versionProperty.getValue(entity)));
		entityManager.refresh(entity);
		assertTrue(versionValue.equals(versionProperty.getValue(entity)));
		
		tx = entityManager.beginTransaction();
		entityManager.put(tx, entity);
		tx.commit();
		versionValue = versionProperty.getValue(entity);
		entityManager.refresh(entity);
		assertTrue(versionValue.equals(versionProperty.getValue(entity)));
		
	}
	
	@Test(expected=ConcurrentModificationException.class)
	public void testSinglePutTransactionCollision() {
		T entity = createEntity();
		entityManager.put(entity);
		
		// a copy of the entity object
		T entity2 = entityManager.get(repository.get(entity.getClass()).getKeyProperty().getValue(entity));
		
		Object versionValue = versionProperty.getValue(entity);
		
		Transaction tx1 = entityManager.beginTransaction();
		entityManager.put(tx1, entity);
		
		Transaction tx2 = entityManager.beginTransaction();
		entityManager.put(tx2, entity2);
		tx1.commit();
		tx2.commit();
	}
	
	@Test
	public void testMultiplePutSuccess() {
		List entities = createEntities();
		entityManager.put(entities);
		
		// first value
		Object startValue = versionProperty.getValue(entities.get(0));
		assertNotNull(startValue);
		
		// second value
		entityManager.put(entities);
		assertFalse(startValue.equals(versionProperty.getValue(entities.get(0))));
	}
	
	@Test(expected=OptimisticLockException.class)
	public void testMultiplePutOptimisticLockNull() {
		List entities = createEntities();
		entityManager.put(entities);
		entityManager.put(entities); // second update because of the primitive test
		
		versionProperty.setValue(entities.get(0), defaultValue);
		entityManager.put(entities);
	}
	
	@Test(expected=OptimisticLockException.class)
	public void testMultiplePutOptimisticLockNotNull() {
		List entities = createEntities();
		entityManager.put(entities);
		Object startValue = versionProperty.getValue(entities.get(0));
		
		entityManager.put(entities);
		versionProperty.setValue(entities.get(0), startValue);
		entityManager.put(entities);
	}
	
	@Test
	public void testMultiplePutTransaction() {
		List entities = ImmutableList.of(createEntity());
		entityManager.put(entities);
		Object versionValue = versionProperty.getValue(entities.get(0));
		
		Transaction tx = entityManager.beginTransaction();
		entityManager.put(tx, entities);
		tx.rollback();
		assertFalse(versionValue.equals(versionProperty.getValue(entities.get(0))));
		entityManager.refresh(entities.get(0));
		assertTrue(versionValue.equals(versionProperty.getValue(entities.get(0))));
		
		tx = entityManager.beginTransaction();
		entityManager.put(tx, entities);
		tx.commit();
		versionValue = versionProperty.getValue(entities.get(0));
		entityManager.refresh(entities.get(0));
		assertTrue(versionValue.equals(versionProperty.getValue(entities.get(0))));
		
	}
	
	
	
}
