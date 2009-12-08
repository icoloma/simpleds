package org.simpleds.tx;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;


/**
 * Interceptor configured using spring to get automatic transaction management 
 * with spring services
 * @author Nacho
 *
 */
@Aspect
public class TransactionInterceptor {

	@AfterReturning(
			"execution(* *(..)) " +
			"and @annotation(transactional) "
			//"and args(transactional)"
			)
	public void doCommit(Transactional transactional) {
		EntityManagerFactory.getEntityManager().commit();
	}
	
	@AfterThrowing(throwing="exception", pointcut=
			"execution(* *(..)) " +
			"and @annotation(transactional) " 
			//"and args(transactional, exception)"
	)
	public void doRollback(Transactional transactional, Exception exception) {
		EntityManager entityManager = EntityManagerFactory.getEntityManager();
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
			entityManager.rollback();
		} else {
			entityManager.commit();
		}
	}
	
}
