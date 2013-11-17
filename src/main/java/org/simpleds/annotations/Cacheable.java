package org.simpleds.annotations;

import org.simpleds.cache.CacheManager;

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
     * The Memcache namespace to store entities. The default is {@link org.simpleds.cache.CacheManager#ENTITIES_NAMESPACE}.
     */
    String namespace() default CacheManager.ENTITIES_NAMESPACE;

	/** 
	 * The number of seconds that this class should be cached in Memcache.
	 * If not specified, the class will be stored in Level 1 but not in Level 2 cache.
	 */
	int value() default 0;
	
}
