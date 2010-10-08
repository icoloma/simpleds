package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.simpleds.testdb.Dummy1;

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
			.sortAsc("name")
			.withChunkSize(3);
		CursorList<Dummy1> list = query.asCursorList();
		assertNotNull(list.getCursor());
		assertEquals(3, list.getData().size());
		assertEquals("2", list.getData().get(2).getName());
		
		// test second page of results
		list = query.withStartCursor(list.getCursor()).asCursorList();
		assertNull(list.getCursor());
		assertEquals(3, list.getData().size());
		assertEquals("5", list.getData().get(2).getName());
		
	}

}
