package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Set;

import com.google.appengine.api.datastore.Key;

/**
 * Annotation to specify a single Relation Index for one persistent entity.
 * Relation indexes have been explained at Google I/O 2009: http://www.youtube.com/watch?v=AgaL6NGpkB8
 * By using this annotation, you can specify a persistent attribute that will contain a list or 
 * set of the specified type, but that will not be stored/retrieved with the persistent entity 
 * itself, thus saving the serialization/deserialization costs. 
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface RelationIndex {

	/** the name of this index */
	String value();
	
	/** the type of the collection container, defaults to Set. SortedSet, Set and List are supported */
	Class<? extends Collection> collectionClass() default Set.class;
	
	/** the type of each item inside the collection, defaults to Key. Any simple data type is supported */
	Class<?> itemClass() default Key.class;
	
	
	
}
