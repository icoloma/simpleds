package org.simpleds.tx;

import java.io.IOException;

public interface TransactionalService {

	public void saveSuccess(boolean shouldRollback);
	
	public void saveFailure(boolean shouldRollback);
	
	public void saveWithException(boolean shouldRollback);
	
	public void saveWithException2(boolean shouldRollback) throws IOException;
	
}
