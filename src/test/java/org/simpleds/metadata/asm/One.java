package org.simpleds.metadata.asm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies the annotated method as a many-to-one or one-to-one relationship between two entities.
 *  
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface One {	
	
	/**
	 * Indicates the attribute name used for this relationship.
	 * If not set, the default name will be calculated by appending "Key" to the getter attribute name.
	 * <pre>
	 * // this example will use a persistent attribute "masterKey"
	 * @One
	 * public Master getMaster() {}
	 * </pre>
	 */
	String value() default "";
	
}
