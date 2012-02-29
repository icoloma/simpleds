package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Annotation to specify a detached multivalued index for one persistent entity.
 * Relation indexes have been explained at <a href="http://www.youtube.com/watch?v=AgaL6NGpkB8">Google I/O 2009</a>
 * <p>
 * By using this annotation, you can specify a persistent attribute that will 
 * contain a collection of items of the specified type, but that will not be 
 * stored/retrieved with the persistent entity itself, thus saving the 
 * serialization/deserialization costs.
 *  
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface MultivaluedIndex {

	/** The name of this index */
	String name();
	
	/** The type of the collection container, defaults to Set. SortedSet, Set and List are supported */
	Class<?> collectionClass() default Set.class;
	
	/** The type of each item inside the collection. Any simple data type is supported */
	Class<?> itemClass();
	
}
