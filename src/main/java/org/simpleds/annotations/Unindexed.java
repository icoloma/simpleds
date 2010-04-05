package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

/**
 * Marks an attribute as unindexed. Unindexed attributes do not create an automatic 
 * index for itself alone. The default for all attributes in GAE is being indexed
 * except for {@link Text} and {@link Blob}.
 * <p>
 * Details about this can be found here:
 * <ul>
 * <li><a href="http://code.google.com/appengine/articles/storage_breakdown.html">The GAE Official docs</a></li>
 * <li><a href="http://groups.google.com/group/google-appengine-java/browse_thread/thread/80b863dcaec0b525/9547591e4bf278e7?lnk=gst&q=objectify#9547591e4bf278e7">The Objectify 2.1 release notes</a></li>
 * </ul>
 * 
 * 
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Unindexed {
}
