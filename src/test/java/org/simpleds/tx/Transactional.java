package org.simpleds.tx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is considered transactional, that is, all managed 
 * transactions should be commited or rolled back after method exit. 
 * By default transactions will be rolled back on any exception, and commited otherwise
 * @author Nacho
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {
	
	/**
	 * Indicates which exception types must cause
	 * a transaction rollback.
	 */
	Class<? extends Throwable>[] rollbackFor() default {};
	
	/**
	 * Indicates which exception types must not cause
	 * a transaction rollback.
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};
	
}
