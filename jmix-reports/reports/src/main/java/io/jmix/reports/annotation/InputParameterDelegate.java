package io.jmix.reports.annotation;

import io.jmix.reports.delegate.ParameterTransformer;
import io.jmix.reports.delegate.ParameterValidator;
import io.jmix.reports.yarg.structure.DefaultValueProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks a delegate method that implements some logic related to an input parameter
 *   defined in the report definition class.
 * Method must conform to convention: no parameters, the result is one of supported functional interfaces.
 * Currently supported interfaces:
 * <li>{@link DefaultValueProvider} - provides default value for the parameter</li>
 * <li>{@link ParameterValidator} - validates parameter value</li>
 * <li>{@link ParameterTransformer} - transforms parameter value</li>
 * @see InputParameterDef
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InputParameterDelegate {
    /**
     * Alias of the input parameter declared in the current report definition.
     */
    String alias();
}
