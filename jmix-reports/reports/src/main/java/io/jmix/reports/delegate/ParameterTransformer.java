package io.jmix.reports.delegate;

import java.util.Map;

/**
 * Transformer can process input parameter values before they are passed to the reporting engine.
 * Examples of possible transformations: ordering a collection, rounding date-time to the nearest day.
 * @param <T> class of the input parameter
 */
@FunctionalInterface
public interface ParameterTransformer<T> {

    /**
     * Transform input parameter's value, possibly even changing its type.
     * @param value source value
     * @param parameterValues map of all input parameters
     * @return transformed value
     */
    Object transform(T value, Map<String, Object> parameterValues);
}
