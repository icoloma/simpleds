package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a persistent class as cacheable.
 * 
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface Cacheable {
	
	/** 
	 * The number of seconds that this class should be cached in memcache.
	 * If not specified, the class will be stored in Level 1 cache but not in Level 2 
	 */
	int value() default 0;
	
}
