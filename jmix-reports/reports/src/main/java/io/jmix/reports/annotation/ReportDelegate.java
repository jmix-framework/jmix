package io.jmix.reports.annotation;

import io.jmix.reports.delegate.ParametersCrossValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks a delegate method that implements some logic related to the report
 *   defined by the current class.
 * Method must conform to convention: no parameters, the result is one of supported functional interfaces.
 * Currently supported interfaces:
 * <li>{@link ParametersCrossValidator} - for parameters cross-validation</li>
 *
 * @see ReportDef
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportDelegate {
}
