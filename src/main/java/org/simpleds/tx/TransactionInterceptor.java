package org.simpleds.tx;

import javax.annotation.PostConstruct;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.simpleds.annotations.Transactional;

import com.google.appengine.api.datastore.DatastoreServiceFactory;


/**
 * Interceptor configured using aspects to get automatic transaction management 
 * for methods annotated as {@link Transactional}
 * @author Nacho
 *
 */
@Aspect
public class TransactionInterceptor {

	private TransactionManager transactionManager;
	
	@PostConstruct
	public void initTransactionManager() {
		if (transactionManager == null) {
			transactionManager = new TransactionManagerImpl();
			((TransactionManagerImpl)transactionManager).setDatastoreService(DatastoreServiceFactory.getDatastoreService());
		}
	}
	
	@AfterReturning(
			"execution(* *(..)) and @annotation(transactional) "
			)
	public void doCommit(Transactional transactional) {
		transactionManager.commit();
	}
	
	@AfterThrowing(throwing="exception", pointcut=
			"execution(* *(..)) and @annotation(transactional) " 
	)
	public void doRollback(Transactional transactional, Exception exception) {
		transactionManager.rollback();
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
}
