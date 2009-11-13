package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the class is an entity. This annotation can be used instead of the JPA
 * annotation, as it includes more information useful for schema validation.
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
	 */
	Class<?>[] parent() default {};
	
}
