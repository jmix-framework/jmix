package io.jmix.flowui.screen;

import io.jmix.flowui.SameAsUi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for declarative handler methods in UI controllers.
 * <br>
 * Example:
 * <pre>
 *    &#64;Install(to = "label", subject = "formatter")
 *    protected String formatValue(Integer value) {
 *        // the method used as Label formatter
 *        return "1.0";
 *    }
 * </pre>
 *
 * @see Screen
 */
@SameAsUi
@Retention(RetentionPolicy.RUNTIME)
@Documented
@java.lang.annotation.Target(ElementType.METHOD)
public @interface Install {
    /**
     * @return type of target
     */
    Target target() default Target.COMPONENT;

    /**
     * @return type of functional interface, can be used instead of {@link #subject()}
     */
    Class<?> type() default Object.class;

    /**
     * @return property name that will be set using annotated method
     */
    String subject() default "";

    /**
     * @return id or path to target object
     */
    String to() default "";

    /**
     * Declares whether the annotated dependency is required.
     * <p>Defaults to {@code true}.
     */
    boolean required() default true;
}