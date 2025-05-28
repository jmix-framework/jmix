package io.jmix.reports.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that marked class is a report group definition.
 * Report group described by this definition can be used to group semantically related reports together.
 * The group will be available in the running application for observing in UI and REST API.
 * <br/>
 * Report definition is a Spring bean (which isn't really used for now).
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportGroup}.
 *
 * @see ReportDef
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ReportGroupDef {

    /**
     * Group title.
     * Use <code>msg://group/key</code> format if localization is required.
     */
    String title();

    /**
     * Unique group code, may be used to identify the group when referring to it programmatically.
     */
    String code();

    /**
     * Optional unique id in the UUID format.
     * Specify this attribute to have stable object id in runtime (e.g. for URL routes).
     */
    String uuid() default "";

    /**
     * Name of the Spring bean containing group definition.
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String beanName() default "";
}
