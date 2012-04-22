package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.simpleds.cache.CacheFilter;

/**
 * Annotates a persistent class. 
 * 
 * @see CacheFilter
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface Entity {

	/** 
	 * The parent class. Indicates the possible parent types when generating 
	 * key values for this class. Leave empty for root entities. 
	 * @deprecated use {@link Id} instead
	 * /
	Class<?>[] parent() default {};
	*/
	
	/**
	 * The kind name to use in the datastore to identify this entity. Defaults to the 
	 * unqualified class name.
	 * This can be used to use shorter names that save space in the datastore. 
	 */
	String value() default "";
	
}
