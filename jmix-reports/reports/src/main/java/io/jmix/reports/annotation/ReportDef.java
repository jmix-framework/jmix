package io.jmix.reports.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that marked class is a report definition.
 * Report described by this definition will be available in the running application for observing and running it:
 *  in UI, REST API or programmatically.
 * Report definition contains of element annotations and optional delegate methods.
 * <br/>
 * Element annotations define report structure containing from input parameters, bands, data sets, templates and value formats.
 * They are also put on the class.
 * <br/>
 * Delegate methods are methods implementing some logic for the related report element, such as data loading or parameter validation.
 * These methods are declared in the class body and annotated with {@link InputParameterDelegate} or similar annotations.
 * <br/>
 * Report definition is a Spring bean, so it can autowire dependencies necessary for implementing logic in delegates.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.Report}.
 * @see InputParameterDef
 * @see BandDef
 * @see TemplateDef
 * @see ValueFormatDef
 * @see ReportDelegate
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ReportDef {

    /**
     * Report name.
     * Use <code>msg://group/key</code> format if localization is required.
     */
    String name();

    /**
     * Unique report code. May be used as a unique identifier in APIs.
     */
    String code();

    /**
     * Name of the Spring bean containing report definition.
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String beanName() default "";

    /**
     * Detailed report description.
     */
    String description() default "";

    /**
     * Optional unique id in the UUID format.
     * Specify this attribute to have stable object id in runtime (e.g. for URL routes).
     */
    String uuid() default "";

    /**
     * Class of the report group definition.
     * @see ReportGroupDef
     */
    Class<?> group();

    /**
     * Whether the report should be accessible via REST API.
     */
    boolean restAccessible() default false;

    /**
     * Whether the report is only for internal system use, i.e. it should be hidden in UI.
     */
    boolean system() default false;

    /**
     * Groovy script to perform cross-parameter validation of input parameters.
     */
    String crossValidationScript() default "";
}
