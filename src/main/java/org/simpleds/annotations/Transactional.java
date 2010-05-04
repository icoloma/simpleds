package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.simpleds.tx.TransactionInterceptor;

/**
 * Indicates that a method is considered transactional, that is, all managed 
 * transactions should be commited or rolled back after method exit. 
 * 
 * @see TransactionInterceptor
 * @author Nacho
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {
	
}
