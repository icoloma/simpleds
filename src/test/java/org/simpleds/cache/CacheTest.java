package org.simpleds.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.testdb.CacheableEntity;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.ImmutableList;

public class CacheTest extends AbstractEntityManagerTest {

	private CacheableEntity cachedEntity;
	private CacheableEntity noncachedEntity;
	
	private CacheManager cacheManager;
	
	private ClassMetadata metadata;
	
	@Before
	public void initCachedData() {
		cacheManager = entityManager.getCacheManager();
		Level1Cache.setCacheInstance();
		cachedEntity = CacheableEntity.create();
		noncachedEntity = CacheableEntity.create();
		entityManager.put(ImmutableList.of(cachedEntity, noncachedEntity));
		cacheManager.delete(noncachedEntity.getKey());
		metadata = entityManager.getClassMetadata(CacheableEntity.class);
		assertTrue(metadata.isCacheable());
		assertEquals(120, metadata.getCacheSeconds());
	}
	
	@After
	public void clearLevel1() {
		Level1Cache.clearCacheInstance();
	}
	
	@Test
	public void testMultipleGetWithLevel1() {
		assertGet();
	}
	
	@Test
	public void testMultipleGetWithoutLevel1() {
		Level1Cache.clearCacheInstance();
		assertGet();
	}
	
	@Test
	public void testSingleGetWithLevel1() {
		assertSame(cachedEntity, entityManager.get(cachedEntity.getKey()));
		CacheableEntity nc2 = entityManager.get(noncachedEntity.getKey());
		assertNotSame(noncachedEntity, nc2);
		assertSame(nc2, entityManager.get(noncachedEntity.getKey()));
	}
	
	@Test
	public void testMultipleDelete() {
		entityManager.delete(cachedEntity.getKey(), noncachedEntity.getKey());
		assertNotInCache(cachedEntity);
		assertNotInCache(noncachedEntity);
	}
	
	@Test
	public void testSingleDelete() {
		assertInCache(cachedEntity);
		entityManager.delete(cachedEntity.getKey());
		assertNotInCache(cachedEntity);
	}
	
	@Test
	public void testSinglePutWithLevel1() {
		assertInCache(cachedEntity);
		CacheableEntity overriden = CacheableEntity.create();
		overriden.setKey(cachedEntity.getKey());
		entityManager.put(overriden);
		assertSame(overriden, entityManager.get(cachedEntity.getKey()));
	}
	
	@Test
	public void testSinglePutWithoutLevel1() {
		Level1Cache.clearCacheInstance();
		assertInCache(cachedEntity);
		CacheableEntity overriden = CacheableEntity.create();
		overriden.setName("xxx");
		overriden.setKey(cachedEntity.getKey());
		entityManager.put(overriden);
		CacheableEntity read = entityManager.get(overriden.getKey());
		assertEquals("xxx", read.getName());
	}
	
	@Test
	public void testNotCacheableData() {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		memcache.setNamespace(CacheManager.MEMCACHE_NAMESPACE);
		memcache.clearAll();
		Dummy1 dummy = Dummy1.create();
		entityManager.put(dummy);
		entityManager.put(ImmutableList.of(dummy, dummy));
		entityManager.get(dummy.getKey());
		entityManager.get(ImmutableList.of(dummy.getKey(), dummy.getKey()));
		assertEquals(0, memcache.getStatistics().getItemCount());
	}

	private void assertGet() {
		// assert cache contents
		assertInCache(cachedEntity);
		assertNotInCache(noncachedEntity);
		
		// retrieve data
		List<CacheableEntity> readData = entityManager.get(ImmutableList.of(cachedEntity.getKey(), noncachedEntity.getKey()));
		assertEquals(2, readData.size());
		assertInCache(cachedEntity);
		assertInCache(noncachedEntity);
		assertNotSame(noncachedEntity, readData.get(1));
		if (Level1Cache.getCacheInstance() != null) {
			assertSame(cachedEntity, readData.get(0));
		} else {
			assertNotSame(cachedEntity, readData.get(0));
		}
		
		// repeat read if Level1 is set
		if (Level1Cache.getCacheInstance() != null) { // there is a level 1 cache, check level1 vs level2
			List<CacheableEntity> readData2 = entityManager.get(ImmutableList.of(cachedEntity.getKey(), noncachedEntity.getKey()));
			assertSame(readData.get(0), readData2.get(0));
			assertSame(readData.get(1), readData2.get(1));
		} 
	}
	
	private void assertInCache(CacheableEntity entity) {
		assertNotNull(cacheManager.get(entity.getKey(), metadata));
	}
	private void assertNotInCache(CacheableEntity entity) {
		assertNull(cacheManager.get(entity.getKey(), metadata));
	}
	
}
