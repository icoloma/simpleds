package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.simpleds.functions.EntityToPropertyFunction;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.Dummy1;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class CursorListTest extends AbstractEntityManagerTest {
	
	@Test
	public void testCursor() throws Exception {
		List<Dummy1> dummies = Lists.newArrayList();
		for (int i = 0; i < 6; i++) {
			Dummy1 d = Dummy1.create();
			d.setName("" + i);
			dummies.add(d);
		}
		entityManager.put(dummies);

		// test first page of results
		SimpleQuery query = entityManager.createQuery(Dummy1.class)
			.sortAsc(Attrs.NAME);
		CursorList<Dummy1> list = query.asCursorList(3);
		assertNotNull(list.getCursor());
		assertEquals(3, list.getData().size());
		assertEquals("2", list.getData().get(2).getName());
		
		// test second page of results
		list = query.withStartCursor(list.getCursor()).asCursorList(3);
		assertNull(list.getCursor());
		assertEquals(3, list.getData().size());
		assertEquals("5", list.getData().get(2).getName());
		
	}
	
	@Test
	public void testTransform() {
		entityManager.put(Dummy1.create());
		CursorList<Dummy1> list = entityManager.createQuery(Dummy1.class).asCursorList(10);
		assertEquals(1, list.getData().size());
			
		// just transform
		EntityToPropertyFunction entityToName = new EntityToPropertyFunction(Dummy1.class, Attrs.NAME);
		CursorList<String> names = list.transform(entityToName);
		assertTrue(names.getData().get(0) instanceof String);
		
		// transform and filter
		CursorList<String> emptyList = list.transform(entityToName, new Predicate<String>() {

			@Override
			public boolean apply(String name) {
				return !"foo".equals(name);
			}
			
		});
		assertTrue(emptyList.getData().isEmpty());
		
		// just filter
		list = list.transform(null, new Predicate<Dummy1>() {

			@Override
			public boolean apply(Dummy1 dummy1) {
				return !"foo".equals(dummy1.getName());
			}
		});
		assertTrue(emptyList.getData().isEmpty());
	}

}
