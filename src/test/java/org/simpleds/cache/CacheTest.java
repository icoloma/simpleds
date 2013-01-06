package org.simpleds.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.KeyFactory2;
import org.simpleds.SimpleQuery;
import org.simpleds.CursorIterator;
import org.simpleds.exception.EntityNotFoundException;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.CacheableEntity;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.ImmutableList;

public class CacheTest extends AbstractEntityManagerTest {

	private CacheableEntity cachedEntity;
	private CacheableEntity noncachedEntity;
	
	private CacheManager cacheManager;
	
	private ClassMetadata metadata;

	@Before
	public void initCachedData() throws Exception {
		
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
	public void testSingleGetWithTransaction() {
		// shopuld ignore the cache settings
		Transaction tx = entityManager.beginTransaction();
		try {
			assertNotSame(cachedEntity, entityManager.get(tx, cachedEntity.getKey()));
		} finally {
			tx.commit();
		}
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
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService(CacheManager.MEMCACHE_NAMESPACE);
		memcache.clearAll();
		Dummy1 dummy = Dummy1.create();
		entityManager.put(dummy);
		entityManager.put(ImmutableList.of(dummy, dummy));
		entityManager.get(dummy.getKey());
		entityManager.get(ImmutableList.of(dummy.getKey(), dummy.getKey()));
		assertEquals(0, memcache.getStatistics().getItemCount());
	}
	
	@Test
	public void testMultipleGetDoesNotExist() {
		Map<Key, CacheableEntity> result = entityManager.get(ImmutableList.of(KeyFactory2.createKey(CacheableEntity.class, 1234)));
		assertTrue(result.isEmpty());
	}
	
	@Test(expected=EntityNotFoundException.class)
	public void testSingleGetDoesNotExist() {
		entityManager.get(KeyFactory2.createKey(CacheableEntity.class, 1234));
	}
	
	@Test
	public void testCalculateCacheKey() throws Exception {
		// empty
		assertCacheKeys(
				"qdata{kind=d1}", 
				entityManager.createQuery(Dummy1.class));
		
		// filter conditions
		assertCacheKeys(
				"qdata{kind=d1,pred=[d = Thu Jan 01 00:00:00 UTC 1970, ev > NULL, n IN [foo, bar], n > bar]}",
				entityManager.createQuery(Dummy1.class)
					.equal(Attrs.DATE, new Date(100))
					.isNotNull(Attrs.E_VALUE)
					.in(Attrs.NAME, ImmutableList.of("foo", "bar"))
					.greaterThan(Attrs.NAME, "bar")
		);
		
		// cursor
		entityManager.put(Dummy1.create());
		CursorIterator<Object> it = entityManager.createQuery(Dummy1.class).asIterator();
		it.next();
		Cursor cursor = it.getCursor();
		assertCacheKeys(
				"qdata{kind=d1,start=E-ABAIICEGoEdGVzdHIICxICZDEYAwwU,end=E-ABAIICEGoEdGVzdHIICxICZDEYAwwU}", 
				entityManager.createQuery(Dummy1.class)
					.withStartCursor(cursor)
					.withEndCursor(cursor)
		);
		
		// fetchOptions
		assertCacheKeys(
				"qdata{kind=d1,off=5,lim=100}", 
				entityManager.createQuery(Dummy1.class)
					.withOffset(5)
					.withLimit(100)
					);
	}
	
	@Test
	public void testCacheQuerySingleResult() {
		singleWithSeconds(0, true);
		singleWithSeconds(10, true);
		singleWithSeconds(10, false);
	}
	
	private void singleWithSeconds(int cacheSeconds, boolean withLevel1) {
		if (withLevel1) {
			Level1Cache.setCacheInstance();
		} else {
			Level1Cache.clearCacheInstance();
		}
		
		entityManager.put(ImmutableList.of(Dummy1.create()));
		SimpleQuery query = entityManager.createQuery(Dummy1.class)
			.greaterThan(Attrs.DATE, new Date(1000))
			.withCacheSeconds(cacheSeconds);
		
		// cache failure
		Dummy1 dummy1 = query.asSingleResult();
		assertNotNull(dummy1);
		dummy1.setOverridenNameDate(new Date(100));
		entityManager.put(dummy1);
		
		// cache 1 hit
		Dummy1 dummy2 = query.asSingleResult();
		assertNotNull(dummy2);
		assertEquals(dummy1.getKey(), dummy2.getKey());

		// cache 2 hit
		if (withLevel1) { 
			// clear level 1
			Level1Cache.setCacheInstance();
		}
		if (cacheSeconds != 0) { // found in level 2 cache
			dummy2 = query.asSingleResult();
			assertEquals(dummy1.getKey(), dummy2.getKey());
		} else { // no level 2 cache
			try {
				query.asSingleResult();
				fail("Found an out of date entity");
			} catch (EntityNotFoundException e) {
				// ok
			}
			
		}

		// cache delete
		try {
			query.clearCache();
			query.asSingleResult();
			fail("Found an out of date entity");
		} catch (EntityNotFoundException e) {
			// ok
		}
	}
	
	@Test
	public void testCacheQueryMultipleResult() {
		cacheSeconds(0, true);
		cacheSeconds(10, true);
		cacheSeconds(10, false);
	}

    @Test
    public void testPopulate() {
        SimpleQuery query = entityManager.createQuery(CacheableEntity.class)
                .equal(Attrs.NAME, "xxxyyyzzz")
                .withCacheSeconds(100)
                ;
        query.populateCache(ImmutableList.of(cachedEntity.getKey()));
        CacheableEntity result = query.asSingleResult();
        assertEquals(result.getKey(), cachedEntity.getKey());
    }
	
	private void cacheSeconds(int cacheSeconds, boolean withLevel1) {
		if (withLevel1) {
			Level1Cache.setCacheInstance();
		} else {
			Level1Cache.clearCacheInstance();
		}

		entityManager.put(ImmutableList.of(Dummy1.create(), Dummy1.create()));
		SimpleQuery query = entityManager.createQuery(Dummy1.class)
			.withCacheSeconds(cacheSeconds);
		List<Dummy1> list = query.asList();
		assertEquals(2, query.count());
		assertEquals(2, list.size());
		
		// insert new data (ignored)
		entityManager.put(ImmutableList.of(Dummy1.create()));
		List<Dummy1> list2 = query.asList();
		assertEquals(2, list2.size());
		assertEquals(list.get(0).getKey(), list2.get(0).getKey());
		
		// clean the cache
		query.clearCache();
		List<Dummy1> list3 = query.asList();
		assertEquals(3, list3.size());
		
		// clean the database
		List<Key> keys = query.keysOnly().asList();
		entityManager.delete(keys);
		query.clearCache();
	}
	

	private void assertGet() {
		// assert cache contents
		assertInCache(cachedEntity);
		assertNotInCache(noncachedEntity);
		
		// retrieve data
		Map<Key, CacheableEntity> readData = entityManager.get(ImmutableList.of(cachedEntity.getKey(), noncachedEntity.getKey()));
		assertEquals(2, readData.size());
		assertInCache(cachedEntity);
		assertInCache(noncachedEntity);
		assertNotSame(noncachedEntity, noncachedEntity.getKey());
		if (Level1Cache.getCacheInstance() != null) {
			assertSame(cachedEntity, readData.get(cachedEntity.getKey()));
		} else {
			assertNotSame(cachedEntity, readData.get(cachedEntity.getKey()));
		}
		
		// repeat read if Level1 is set
		if (Level1Cache.getCacheInstance() != null) { // there is a level 1 cache, check level1 vs level2
			Map<Key, CacheableEntity> readData2 = entityManager.get(ImmutableList.of(cachedEntity.getKey(), noncachedEntity.getKey()));
			assertSame(readData.get(noncachedEntity.getKey()), readData2.get(noncachedEntity.getKey()));
			assertSame(readData.get(cachedEntity.getKey()), readData2.get(cachedEntity.getKey()));
		} 
	}
	
	private void assertInCache(CacheableEntity entity) {
		assertNotNull(cacheManager.get(entity.getKey(), metadata));
	}
	private void assertNotInCache(CacheableEntity entity) {
		assertNull(cacheManager.get(entity.getKey(), metadata));
	}
	
	private void assertCacheKeys(String expectedDataKey, SimpleQuery query) throws Exception {
		assertEquals(expectedDataKey, query.getCacheKey());
	}

}
