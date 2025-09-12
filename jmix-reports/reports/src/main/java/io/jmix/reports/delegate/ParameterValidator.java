package io.jmix.reports.delegate;

import io.jmix.reports.exception.ReportParametersValidationException;

/**
 * Interface for implementing a custom input parameter validator.
 * @param <T> parameter type
 */
public interface ParameterValidator<T> {

    /**
     * Validate input value.
     * If one is invalid - throw <code>ReportParametersValidationException</code>,
     * specifying a message that will be shown to the user.
     *
     * @param value value to validate
     * @throws ReportParametersValidationException if value is invalid
     */
    void validate(T value) throws ReportParametersValidationException;
}
