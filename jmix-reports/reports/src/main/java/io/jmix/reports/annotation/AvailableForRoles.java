package io.jmix.reports.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies set of resource roles that can launch the defined report in certain views and actions.
 * If this annotation is missing, or list of roles is empty - then restriction by role is not activated,
 * i.e. any user can launch the report.
 * Persistent analog is {@link io.jmix.reports.entity.ReportRole}.
 *
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableForRoles {

    /**
     * Resource role codes that should have access to the report.
     */
    String[] roleCodes() default {};

    /**
     * Resource role definition interfaces (marked with @ResourceRole) that should have access to the report.
     */
    Class[] roleClasses() default {};
}
