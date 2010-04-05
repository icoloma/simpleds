package org.simpleds.tx;

import org.simpleds.EntityManager;
import org.simpleds.annotations.Transactional;
import org.simpleds.testdb.Dummy1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionalServiceImpl implements TransactionalService {

	@Autowired
	private EntityManager entityManager;
	
	@Override
	@Transactional
	public void saveSuccess(boolean shouldRollback) {
		saveWithTwoTx();
	}

	@Override
	@Transactional
	public void saveFailure(boolean shouldRollback) {
		saveWithTwoTx();
		throw new SecurityException();
	}

	private void saveWithTwoTx() {
		entityManager.put(entityManager.beginTransaction(), Dummy1.create());
		entityManager.put(entityManager.beginTransaction(), Dummy1.create());
	}
	
}
