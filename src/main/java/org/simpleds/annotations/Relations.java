package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify multiple Index Relationships at one persistent entity.
 * Relation indexes have been explained at Google I/O 2009: http://www.youtube.com/watch?v=AgaL6NGpkB8
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface Relations {

	MultivaluedIndex[] value() default {};
	
}
