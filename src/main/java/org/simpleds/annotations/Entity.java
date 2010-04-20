package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a persistent class. 
 * 
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface Entity {

	/** 
	 * The parent class. Indicates the possible parent types when generating 
	 * key values for this class. Leave empty for root entities. 
	 * @deprecated specify the parent using {@link Id} instead
	 */
	Class<?>[] parent() default {};
	
}
