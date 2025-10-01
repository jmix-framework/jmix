package io.jmix.reports.delegate;

import io.jmix.reports.exception.ReportParametersValidationException;

import java.util.Map;

/**
 * Validator invoked as part of input parameters validation on the UI.
 * Checks that selected values are mutually valid
 * (e.g. it can check that at least one of several parameters must be specified).
 */
@FunctionalInterface
public interface ParametersCrossValidator {

    /**
     * Validate input parameter values.
     * If values are invalid - throw <code>ReportParametersValidationException</code>,
     * specifying a message that will be shown to the user.
     *
     * @param parameterValues parameter values
     * @throws ReportParametersValidationException if values are invalid
     */
    void validateParameters(Map<String, Object> parameterValues) throws ReportParametersValidationException;
}
