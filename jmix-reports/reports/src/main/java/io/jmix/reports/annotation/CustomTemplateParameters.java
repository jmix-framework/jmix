package io.jmix.reports.annotation;

import io.jmix.reports.entity.CustomTemplateDefinedBy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines parameters of the custom report template as part of the {@link TemplateDef} definition.
 * Custom template can be used to provide custom implementation for generation of the output document
 * based on band data and value formats.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportTemplate}.
 *
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTemplateParameters {

    /**
     * @return true if a custom template implementation should be used
     */
    boolean enabled() default false;

    /**
     * Specify how custom template is invoked.
     * Note: SCRIPT and CLASS are not supported here, use DELEGATE instead.
     */
    CustomTemplateDefinedBy definedBy() default CustomTemplateDefinedBy.SCRIPT;

    /**
     * Script that accepts parameters map and returns a URL that be called for to generate the output.
     */
    String urlScript() default "";
}
