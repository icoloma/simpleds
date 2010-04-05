package org.simpleds.tx;


public interface TransactionalService {

	public void saveSuccess(boolean shouldRollback);
	
	public void saveFailure(boolean shouldRollback);
	
}
