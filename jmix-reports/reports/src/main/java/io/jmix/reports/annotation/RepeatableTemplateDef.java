package io.jmix.reports.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container for template definitions, if the report has several templates.
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatableTemplateDef {
    TemplateDef[] value() default {};
}
