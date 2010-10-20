package org.simpleds.guice;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.simpleds.tx.TransactionManager;

public class GuiceTransactionInterceptor implements MethodInterceptor {

	@Inject
	private TransactionManager transactionManager;

	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		try {
			Object result = invocation.proceed();
			transactionManager.commit();
			return result;
		} catch (Exception e) {
			transactionManager.rollback();
			throw e;
		}
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

}