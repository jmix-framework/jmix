package io.jmix.reports.yarg.structure;

/**
 * Interface implemented by clients that need to provide a dynamic default value for a input parameter,
 *   or default value for a complex data type that can't be expressed with annotation.
 *
 * @param <T> parameter class
 * @see ReportParameter
 */
@FunctionalInterface
public interface DefaultValueProvider<T> {

    /**
     * Obtain default value for the input parameter.
     *
     * @param parameter input parameter
     * @return default value, already converted into necessary data type
     */
    T getDefaultValue(ReportParameter parameter);
}
