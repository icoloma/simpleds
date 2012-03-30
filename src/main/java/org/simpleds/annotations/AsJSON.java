package org.simpleds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.appengine.api.datastore.Text;

/**
 * Marks a property to be stored as its JSON representation. The stored {@link Text} attribute will be unindexed has a size limit of 1000000 bytes.
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface AsJSON {
}
