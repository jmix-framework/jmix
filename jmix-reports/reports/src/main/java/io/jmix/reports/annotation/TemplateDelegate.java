package io.jmix.reports.annotation;

import io.jmix.reports.yarg.formatters.CustomReport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks a delegate method that implements some logic related to a template
 *   defined in the report definition class.
 * Method must conform to convention: no parameters, the result is one of supported functional interfaces.
 * Currently supported interfaces:
 * <li>{@link CustomReport} - delegates custom generation of the report output document</li>
 *
 * @see TemplateDef
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateDelegate {

    /**
     * Code of the template declared in the current report definition.
     */
    String code();
}
