package io.jmix.reports.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines in which views the defined report will be available when invoking standard reporting actions
 *   (from the io.jmix.reportsflowui.action package).
 * Persistent analog is {@link io.jmix.reports.entity.ReportScreen}.
 *
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableInViews {

    /**
     * IDs of views where this report will be available via standard actions.
     */
    String[] viewIds() default {};

    /**
     * View controllers where this report will be available via standard actions.
     */
    Class[] viewClasses() default {};
}
