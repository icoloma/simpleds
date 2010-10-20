package org.simpleds.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Marks the injection point of the set of persistent classes
 * @author icoloma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
@Qualifier
public @interface PersistentClasses {

}
