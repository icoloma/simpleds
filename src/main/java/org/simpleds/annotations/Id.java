package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the primary key attribute.
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Id {
	
	/**
	 * If set to true, the primary key will be assigned automatically. Default is false.
	 */
	boolean generated() default false;
	
	/** 
	 * The parent class. Indicates the possible parent types when generating 
	 * key values for this class. Leave empty for root entities. 
	 */
	Class<?>[] parent() default {};

}
