package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.simpleds.exception.OptimisticLockException;

/**
 * Mark a property to be used as indicator to implement optimistic locking.
 * If used with a long or Long attribute, it will be used as an incrementing counter.
 * If used with a Date attribute, it will behave as a last-modified feature.
 * In either case, if the object is stored within a transaction, the up-to-date value 
 * will be retrieved when put() is invoked,  and if it doesn't match an {@link OptimisticLockException}
 * will be raised. 
 * is invoked, resulting    
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Version {
	
}
