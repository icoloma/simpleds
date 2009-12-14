package org.simpleds.tx;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;
import org.simpleds.annotations.Transactional;


/**
 * Interceptor configured using spring to get automatic transaction management 
 * with spring services
 * @author Nacho
 *
 */
@Aspect
public class TransactionInterceptor {

	@Before(
			"execution(* *(..)) and @annotation(transactional) "
	)
	public void beforeExecute(Transactional transactional) {
		TransactionManager transactionManager = EntityManagerFactory.getEntityManager().getTransactionManager();
		transactionManager.pushContext();
	}
	
	@AfterReturning(
			"execution(* *(..)) and @annotation(transactional) "
			)
	public void doCommit(Transactional transactional) {
		TransactionManager transactionManager = EntityManagerFactory.getEntityManager().getTransactionManager();
		transactionManager.commit();
	}
	
	@AfterThrowing(throwing="exception", pointcut=
			"execution(* *(..)) and @annotation(transactional) " 
	)
	public void doRollback(Transactional transactional, Exception exception) {
		EntityManager entityManager = EntityManagerFactory.getEntityManager();
		TransactionManager transactionManager = entityManager.getTransactionManager();
		boolean rollback = transactional.rollbackFor().length == 0;
		for (Class<? extends Throwable> exceptionClass : transactional.noRollbackFor()) {
			if (exceptionClass.isAssignableFrom(exception.getClass())) {
				rollback = false;
				break;
			}
		}
		for (Class<? extends Throwable> exceptionClass : transactional.rollbackFor()) {
			if (exceptionClass.isAssignableFrom(exception.getClass())) {
				rollback = true;
				break;
			}
		}
		if (rollback) {
			transactionManager.rollback();
		} else {
			transactionManager.commit();
		}
	}
	
}
