package io.jmix.core.metamodel.annotation;

import io.jmix.core.EntityStates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines properties that the annotated property depends on.
 * <p>
 * These properties are taken into account when building fetch plans and when loading/saving cross-datastore references.
 * Also, if the annotated property is read-only (no setter), {@code EntityPropertyChangeEvent} is sent for this
 * property when the specified properties are changed.
 * <p>
 * Specify only immediate local and reference properties in the annotation.
 * Property paths like {@code customer.name} are not supported.
 * <p>
 * <b>Note:</b> Inclusion of reference or embedded attribute in this annotation may lead to eager loading of it's
 * nested reference attributes during {@link EntityStates#isLoaded} check. It happens if they are used in the annotated method.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOnProperties {

    String[] value() default "";
}
