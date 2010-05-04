package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

/**
 * Specify how a property should get mapped to the Datastore. 
 * All properties are mapped by default unless annotated with {@link Transient}. 
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Property {
	
	/**
	 * The name of the datastore property. If unspecified the java attribute name will be used.
	 */
	String name() default "";
	
	/**
	 * Marks this attribute as required. Default is false.
	 */
	boolean required() default false;
	
	/**
	 * Marks this attribute as unindexed. Unindexed attributes do not create an automatic 
	 * index. 
	 * <p>All attributes are <a href="http://code.google.com/appengine/articles/storage_breakdown.html">indexed by default</a> 
	 * except {@link Text} and {@link Blob} data types.
	 */
	boolean unindexed() default false;
}
