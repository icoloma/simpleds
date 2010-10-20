package org.simpleds.tx;

import javax.inject.Inject;

import org.simpleds.EntityManager;
import org.simpleds.annotations.Transactional;
import org.simpleds.testdb.Dummy1;

public class TransactionalServiceImpl implements TransactionalService {

	@Inject
	private EntityManager entityManager;
	
	@Override
	@Transactional
	public void saveSuccess() {
		saveWithTwoTx();
	}

	@Override
	@Transactional
	public void saveFailure() {
		saveWithTwoTx();
		throw new SecurityException();
	}

	private void saveWithTwoTx() {
		entityManager.put(entityManager.beginTransaction(), Dummy1.create());
		entityManager.put(entityManager.beginTransaction(), Dummy1.create());
	}
	
}
