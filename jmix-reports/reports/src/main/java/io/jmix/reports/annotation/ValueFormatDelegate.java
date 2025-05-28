package io.jmix.reports.annotation;

import io.jmix.reports.yarg.structure.CustomValueFormatter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks a delegate method that implements some logic related to a value format
 *   defined in the report definition class.
 * Method must conform to convention: no parameters, the result is one of supported functional interfaces.
 * Currently supported interfaces:
 * <li>{@link CustomValueFormatter} - custom formatter implementation</li>
 * @see ValueFormatDef
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueFormatDelegate {

    /**
     * "band" attribute of the value format definition.
     */
    String band();

    /**
     * "field" attribute of the value format definition.
     */
    String field();

}
