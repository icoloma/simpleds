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
		throwException(shouldRollback);
	}
	
	@Override
	@Transactional(noRollbackFor=UnsupportedOperationException.class)
	public void saveWithException(boolean shouldRollback) {
		saveWithTwoTx();
		throwException(shouldRollback);
	}
	
	@Override
	@Transactional(rollbackFor=SecurityException.class)
	public void saveWithException2(boolean shouldRollback) {
		saveWithTwoTx();
		throwException(shouldRollback);
	}

	private void throwException(boolean shouldRollback) {
		if (shouldRollback) {
			throw new SecurityException();
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	private void saveWithTwoTx() {
		entityManager.put(entityManager.beginTransaction(), Dummy1.create());
		entityManager.put(entityManager.beginTransaction(), Dummy1.create());
	}
	
}
