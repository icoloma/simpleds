package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

/**
 * Overrides how a property gets mapped on the Datastore. 
 * By default all properties are mapped unless annotated with {@link Transient}. 
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
	 * Marks the annotated attribute as required. Default is false.
	 */
	boolean required() default false;
	
	/**
	 * Marks the annotated attribute as unindexed. Unindexed attributes do not create an automatic 
	 * index. The default for all attributes is being indexed except for {@link Text} and {@link Blob}.
	 * <p>
	 * Details can be found at <a href="http://code.google.com/appengine/articles/storage_breakdown.html">The GAE Official docs</a>.
	 */
	boolean unindexed() default false;
}
