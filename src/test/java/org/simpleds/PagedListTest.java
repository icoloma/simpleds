package org.simpleds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.simpleds.functions.EntityToKeyFunction;
import org.simpleds.functions.KeyToParentKeyFunction;
import org.simpleds.testdb.Child;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Root;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class PagedListTest extends AbstractEntityManagerTest {
	
	@Test
	public void testTransformToProperty() throws Exception {
		List<Dummy1> dummies = Lists.newArrayList();
		for (int i = 0; i < 29; i++) {
			dummies.add(Dummy1.create());
		}
		entityManager.put(dummies);

		PagedQuery query = entityManager.createPagedQuery(Dummy1.class).setPageSize(PagedQueryTest.PAGE_SIZE);
		query.setPageIndex(2);
		PagedList<Dummy1> list = entityManager.findPaged(query);
		assertEquals(29, list.getTotalResults());
		assertEquals(10, list.getTotalPages());
		
		// just transform
		PagedList<String> names = list.transform(new GetDummy1Name());
		assertTrue(names.getData().get(0) instanceof String);
		
		// transform and filter
		PagedList<String> emptyList = list.transform(new GetDummy1Name(), new FilterFooName());
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

	@Test
	public void testTransformFunctions() {
		Key rootKey = entityManager.put(new Root());
		Key childKey = entityManager.put(rootKey, new Child());
		PagedList<Child> children = entityManager.findPaged(entityManager.createPagedQuery(Child.class));
		PagedList<Key> childrenKeys = children.transform(new EntityToKeyFunction<Child>(Child.class));
		PagedList<Key> rootKeys = childrenKeys.transform(new KeyToParentKeyFunction());
		PagedList<Root> rootEntities = rootKeys.transformToEntities();
		assertEquals(1, rootEntities.getTotalResults());
		assertEquals(rootKey, rootEntities.getData().get(0).getKey());
	}
	
	private class GetDummy1Name implements Function<Dummy1, String> {

		@Override
		public String apply(Dummy1 from) {
			return from.getName();
		}
		
	}
	
	private class FilterFooName implements Predicate<String> {

		@Override
		public boolean apply(String name) {
			return !"foo".equals(name);
		}
		
	}
	
}
